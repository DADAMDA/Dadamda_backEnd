package com.example.Oauth.auth.service;

import com.example.Oauth.auth.repository.RefreshTokenRepository;
import com.example.Oauth.auth.token.RefreshToken;
import com.example.Oauth.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public RefreshToken save(User user, String token) {
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60);
        return refreshTokenRepository.save(new RefreshToken(token, user, expiresAt));
    }

    @Transactional(readOnly = true)
    public RefreshToken getValidToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("refresh token revoked");
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken getValidTokenForUpdate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenForUpdate(token)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("refresh token revoked");
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(RefreshToken::revoke);
    }
}