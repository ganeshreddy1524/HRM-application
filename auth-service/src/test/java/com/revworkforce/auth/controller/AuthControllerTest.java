package com.revworkforce.auth.controller;

import com.revworkforce.auth.dto.*;
import com.revworkforce.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void resetPasswordCallsService() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmployeeId("100");
        req.setEmail("a@b.com");
        req.setNewPassword("x");
        var response = controller.resetPassword(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).resetPassword(req);
    }
}
