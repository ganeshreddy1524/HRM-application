package com.revworkforce.auth.controller;

import com.revworkforce.auth.dto.*;
import com.revworkforce.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Auth login attempt username={}", request.getUsername());
        AuthResponse response = authService.login(request);
        log.info("Auth login success username={} userId={} role={}", request.getUsername(), response.getUserId(), response.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Reset password request employeeId={} email={}", request.getEmployeeId(), request.getEmail());
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password reset successful"));
    }

    // Helper DTO classes
    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
