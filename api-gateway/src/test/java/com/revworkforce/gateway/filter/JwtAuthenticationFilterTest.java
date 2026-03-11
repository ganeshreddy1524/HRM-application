package com.revworkforce.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtAuthenticationFilterTest {

    @Test
    void filterAddsUserHeadersForValidJwt() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        String secret = "RevWorkForceSecretKeyForJwtTokenGeneration12345678901234567890";
        ReflectionTestUtils.setField(filter, "jwtSecret", secret);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("12")
                .claim("email", "user@revworkforce.com")
                .claim("role", "EMPLOYEE")
                .signWith(key)
                .compact();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/api/employees/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        AtomicReference<ServerHttpRequest> seenRequest = new AtomicReference<>();
        GatewayFilterChain chain = updatedExchange -> {
            seenRequest.set(updatedExchange.getRequest());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(seenRequest.get());
        assertEquals("12", seenRequest.get().getHeaders().getFirst("X-User-Id"));
        assertEquals("user@revworkforce.com", seenRequest.get().getHeaders().getFirst("X-User-Email"));
        assertEquals("EMPLOYEE", seenRequest.get().getHeaders().getFirst("X-User-Role"));
    }
}
