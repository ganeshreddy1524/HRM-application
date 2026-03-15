package com.revworkforce.auth.controller;

import com.revworkforce.auth.dto.*;
import com.revworkforce.auth.entity.User;
import com.revworkforce.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void loginReturnsOk() {
        LoginRequest req = new LoginRequest();
        req.setUsername("u");
        req.setPassword("p");

        AuthResponse res = AuthResponse.builder().accessToken("t").refreshToken("r").role("ADMIN").fullName("X").userId(1L).build();
        when(authService.login(req)).thenReturn(res);

        var response = controller.login(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("t", response.getBody().getAccessToken());
    }

    @Test
    void refreshTokenReturnsOk() {
        TokenRefreshRequest req = new TokenRefreshRequest();
        req.setRefreshToken("r");
        TokenRefreshResponse res = TokenRefreshResponse.builder().accessToken("a").refreshToken("r2").tokenType("Bearer").build();
        when(authService.refreshToken(req)).thenReturn(res);

        var response = controller.refreshToken(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("a", response.getBody().getAccessToken());
    }

    @Test
    void logoutCallsService() {
        var response = controller.logout(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).logout(10L);
    }

    @Test
    void resetPasswordCallsService() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmployeeId("100");
        req.setEmail("a@b.com");
        req.setNewPassword("x");
        var response = controller.resetPassword(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).resetPassword(req);
    }

    @Test
    void validateTokenExtractsBearerPrefix() {
        UserValidationResponse res = UserValidationResponse.builder().valid(true).userId(1L).role("ADMIN").build();
        when(authService.validateToken("abc")).thenReturn(res);
        var response = controller.validateToken("Bearer abc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isValid());
    }

    @Test
    void validateTokenRejectsInvalidHeader() {
        assertThrows(IllegalArgumentException.class, () -> controller.validateToken("abc"));
    }

    @Test
    void getUserByIdMapsFields() {
        User user = User.builder()
                .id(10L)
                .employeeId("1001")
                .email("a@b.com")
                .fullName("Name")
                .roleId(1)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(authService.getUserById(10L)).thenReturn(user);

        var response = controller.getUserById(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1001", response.getBody().getEmployeeId());
        assertEquals("Name", response.getBody().getFullName());
    }
}

