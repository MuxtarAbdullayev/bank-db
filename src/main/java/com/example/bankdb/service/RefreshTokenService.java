package com.example.bankdb.service;

import com.example.bankdb.exception.RefreshTokenExpiredException;
import com.example.bankdb.exception.RefreshTokenNotFoundException;
import com.example.bankdb.model.entity.RefreshToken;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration:36000000}")
    private Long refreshTokenExpiration;

    public RefreshToken createOrUpdateRefreshToken(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(existing -> {
            log.info("Deleting existing refresh token for user: {}", user.getEmail());
            refreshTokenRepository.delete(existing);
        });

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {}", user.getEmail());
        return saved;
    }

    public boolean isRefreshTokenValid(RefreshToken token) {
        boolean valid = token.getExpiryDate().isAfter(Instant.now());
        if (!valid) {
            log.warn("Refresh token expired for user: {}", token.getUser().getEmail());
        }
        return valid;
    }

    public RefreshToken getValidRefreshToken(String token) {
        log.info("Looking up refresh token: {}", token);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Refresh token not found: {}", token);
                    return new RefreshTokenNotFoundException("Refresh token not found");
                });

        if (!isRefreshTokenValid(refreshToken)) {
            log.error("Refresh token is expired: {}", token);
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        log.info("Refresh token is valid for user: {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }


    public void deleteRefreshToken(RefreshToken token) {
        log.info("Deleting refresh token for user: {}", token.getUser().getEmail());
        refreshTokenRepository.delete(token);
    }
}

