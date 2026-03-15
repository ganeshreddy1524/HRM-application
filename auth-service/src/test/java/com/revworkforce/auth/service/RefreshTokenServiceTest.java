package com.revworkforce.auth.service;

import com.revworkforce.auth.entity.RefreshToken;
import com.revworkforce.auth.exception.AuthException;
import com.revworkforce.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService service;

    @Test
    void createRefreshTokenSavesWithUserId() {
        ReflectionTestUtils.setField(service, "refreshExpiration", 1_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken token = service.createRefreshToken(10L);
        assertEquals(10L, token.getUserId());
        assertNotNull(token.getToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findByTokenDelegatesToRepository() {
        when(refreshTokenRepository.findByToken("t")).thenReturn(Optional.of(RefreshToken.builder().token("t").build()));
        assertEquals(true, service.findByToken("t").isPresent());
    }

    @Test
    void verifyExpirationDeletesAndThrowsWhenExpired() {
        RefreshToken t = RefreshToken.builder()
                .userId(10L)
                .token("t")
                .expiryDate(Instant.now().minusSeconds(1))
                .build();

        assertThrows(AuthException.class, () -> service.verifyExpiration(t));
        verify(refreshTokenRepository).delete(t);
    }

    @Test
    void verifyExpirationReturnsSameTokenWhenValid() {
        RefreshToken t = RefreshToken.builder()
                .userId(10L)
                .token("t")
                .expiryDate(Instant.now().plusSeconds(60))
                .build();
        assertSame(t, service.verifyExpiration(t));
    }

    @Test
    void deleteByUserIdDelegatesToRepository() {
        service.deleteByUserId(10L);
        verify(refreshTokenRepository).deleteByUserId(10L);
    }
}

