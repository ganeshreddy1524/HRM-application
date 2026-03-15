package com.revworkforce.auth.service;

import com.revworkforce.auth.entity.RefreshToken;
import com.revworkforce.auth.exception.AuthException;
import com.revworkforce.auth.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(Long userId) {
        log.debug("Create refresh token userId={}", userId);
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            log.info("Refresh token expired userId={}", token.getUserId());
            refreshTokenRepository.delete(token);
            throw new AuthException("Refresh token has expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        log.debug("Delete refresh tokens userId={}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }
}
