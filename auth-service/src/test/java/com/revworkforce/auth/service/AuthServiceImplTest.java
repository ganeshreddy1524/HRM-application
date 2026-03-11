package com.revworkforce.auth.service;

import com.revworkforce.auth.dto.AuthResponse;
import com.revworkforce.auth.dto.LoginRequest;
import com.revworkforce.auth.entity.RefreshToken;
import com.revworkforce.auth.entity.User;
import com.revworkforce.auth.enums.Role;
import com.revworkforce.auth.repository.RoleMasterRepository;
import com.revworkforce.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleMasterRepository roleMasterRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginSupportsEmployeeIdAsUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername("1001");
        request.setPassword("Password@123");

        User user = User.builder()
                .id(10L)
                .employeeId("1001")
                .email("employee@revworkforce.com")
                .fullName("Employee One")
                .password("encoded")
                .roleId(Role.EMPLOYEE.getId())
                .status("ACTIVE")
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .userId(10L)
                .token("refresh-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(userRepository.findByEmail("1001")).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeId("1001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encoded")).thenReturn(true);
        when(jwtService.generateToken(10L, "employee@revworkforce.com", Role.EMPLOYEE)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(10L)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Employee One", response.getFullName());
        assertEquals("EMPLOYEE", response.getRole());
    }
}
