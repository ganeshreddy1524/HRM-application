package com.revworkforce.auth.service;

import com.revworkforce.auth.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateAndValidateTokenRoundTrip() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(service, "expiration", 60_000L);

        String token = service.generateToken(10L, "a@b.com", Role.ADMIN);
        assertTrue(service.validateToken(token));
        assertEquals(10L, service.extractUserId(token));
        assertEquals("a@b.com", service.extractEmail(token));
        assertEquals("ADMIN", service.extractRole(token));
    }

    @Test
    void validateTokenReturnsFalseForInvalidToken() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(service, "expiration", 60_000L);
        assertFalse(service.validateToken("not-a-jwt"));
    }

    @Test
    void validateTokenReturnsFalseForExpiredToken() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(service, "expiration", -1L);

        String token = service.generateToken(1L, "a@b.com", Role.EMPLOYEE);
        assertFalse(service.validateToken(token));
    }
}

