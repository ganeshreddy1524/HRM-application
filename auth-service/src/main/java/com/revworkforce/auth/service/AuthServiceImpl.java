package com.revworkforce.auth.service;

import com.revworkforce.auth.dto.*;
import com.revworkforce.auth.entity.RefreshToken;
import com.revworkforce.auth.entity.User;
import com.revworkforce.auth.enums.Role;
import com.revworkforce.auth.exception.AuthException;
import com.revworkforce.auth.exception.ResourceNotFoundException;
import com.revworkforce.auth.repository.RoleMasterRepository;
import com.revworkforce.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleMasterRepository roleMasterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleMasterRepository roleMasterRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleMasterRepository = roleMasterRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String username = (request.getUsername() == null ? "" : request.getUsername()).trim();
        if (username.isBlank()) {
            throw new AuthException("Invalid email or password");
        }
        log.info("Login attempt username={}", username);
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByEmployeeId(username))
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed (password mismatch) userId={}", user.getId());
            throw new AuthException("Invalid email or password");
        }

        if ("INACTIVE".equals(user.getStatus())) {
            log.warn("Login rejected (inactive) userId={}", user.getId());
            throw new AuthException("Account is inactive. Please contact administrator.");
        }

        Role role = resolveRole(user.getRoleId());
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), role);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        log.info("Login success userId={} role={}", user.getId(), role.name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role.name())
                .build();
    }



    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        log.info("Reset password attempt employeeId={} email={}", request.getEmployeeId(), request.getEmail());
        User user = userRepository
                .findByEmployeeIdAndEmail(
                        request.getEmployeeId(),
                        request.getEmail()
                )
                .orElseThrow(() -> new AuthException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all refresh tokens for this user
        refreshTokenService.deleteByUserId(user.getId());
        log.info("Reset password success userId={}", user.getId());
    }

    @Override
    public UserValidationResponse validateToken(String token) {
        try {
            if (jwtService.validateToken(token)) {
                Long userId = jwtService.extractUserId(token);
                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                log.debug("Token validated userId={} role={}", userId, role);
                return UserValidationResponse.builder()
                        .userId(userId)
                        .email(email)
                        .fullName(user.getFullName())
                        .role(role)
                        .valid(true)
                        .build();
            }
        } catch (Exception e) {
            log.debug("Token validation failed");
        }

        return UserValidationResponse.builder()
                .valid(false)
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        log.info("Refresh token attempt");
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = resolveRole(user.getRoleId());
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), role);
        log.info("Refresh token success userId={} role={}", user.getId(), role.name());

        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        log.info("Logout userId={}", userId);
        refreshTokenService.deleteByUserId(userId);
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("Get user by id userId={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
    @Override
    @Transactional
    public User createUser(AdminCreateUserRequest request) {
        log.info("Create user attempt employeeId={} email={}", request.getEmployeeId(), request.getEmail());
        // Validate employee ID is numeric
        try {
            Long.parseLong(request.getEmployeeId().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Employee ID must be numeric");
        }

        // Check for duplicate employee ID
        if (userRepository.existsByEmployeeId(request.getEmployeeId().trim())) {
            throw new IllegalArgumentException("Employee ID already exists");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Convert role string to role ID
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = Role.EMPLOYEE;
        }

        User user = User.builder()
                .employeeId(request.getEmployeeId().trim())
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleId(role.getId())
                .status("ACTIVE")
                .build();

        User saved = userRepository.save(user);
        log.info("Create user success userId={} roleId={}", saved.getId(), saved.getRoleId());
        return saved;
    }

    private static Role resolveRole(Integer roleId) {
        if (roleId == null) {
            return Role.EMPLOYEE;
        }
        try {
            return Role.fromId(roleId);
        } catch (IllegalArgumentException ex) {
            return Role.EMPLOYEE;
        }
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Get all users");
        return userRepository.findAll();
    }

    @Override
    public List<User> searchUsers(String query) {
        log.debug("Search users query={}", query);
        return userRepository.searchByNameOrEmployeeId(query);
    }

    @Override
    @Transactional
    public User setUserActive(Long userId, boolean active) {
        log.info("Set user active userId={} active={}", userId, active);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setStatus(active ? "ACTIVE" : "INACTIVE");
        return userRepository.save(user);
    }
}
