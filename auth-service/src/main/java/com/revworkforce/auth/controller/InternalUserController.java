package com.revworkforce.auth.controller;

import com.revworkforce.auth.entity.User;
import com.revworkforce.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Internal service-to-service endpoint (not used by the frontend directly).
 * Used by employee-service to hydrate an employee profile for a userId.
 */
@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private static final Logger log = LoggerFactory.getLogger(InternalUserController.class);

    private final AuthService authService;

    public InternalUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        log.debug("Internal get user userId={}", userId);
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "userId", user.getId(),
                "employeeId", user.getEmployeeId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "roleId", user.getRoleId(),
                "status", user.getStatus()
        ));
    }
}

