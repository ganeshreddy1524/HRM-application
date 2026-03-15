package com.revworkforce.auth.controller;

import com.revworkforce.auth.dto.AdminCreateUserRequest;
import com.revworkforce.auth.dto.UserListResponse;
import com.revworkforce.auth.entity.User;
import com.revworkforce.auth.exception.UnauthorizedException;
import com.revworkforce.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<UserListResponse>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Get all users role={}", role);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admins can view all users");
        }
        List<User> users = authService.getAllUsers();
        List<UserListResponse> response = users.stream()
                .map(UserListResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserListResponse> createUser(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody AdminCreateUserRequest request) {
        log.info("Create user role={} employeeId={}", role, request.getEmployeeId());
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admins can create users");
        }
        User user = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserListResponse(user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserListResponse>> searchUsers(@RequestParam String q) {
        log.info("Search users q={}", q);
        List<User> users = authService.searchUsers(q);
        List<UserListResponse> response = users.stream()
                .map(UserListResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserListResponse> setUserActive(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info("Set user active role={} id={} active={}", role, id, active);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admins can activate/deactivate users");
        }
        User user = authService.setUserActive(id, active);
        return ResponseEntity.ok(new UserListResponse(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserListResponse> getCurrentUser(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Get current user userId={}", userId);
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(new UserListResponse(user));
    }
}
