package com.example.Oauth.auth.service;

import com.example.Oauth.auth.exception.ExpiredTokenException;
import com.example.Oauth.auth.exception.InvalidTokenException;
import com.example.Oauth.auth.exception.RevokedTokenException;
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
    private final TokenCryptoService tokenCryptoService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            TokenCryptoService tokenCryptoService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenCryptoService = tokenCryptoService;
    }

    @Transactional
    public RefreshToken save(User user, String rawRefreshToken, String jti, java.time.Instant expiresAt) {
        refreshTokenRepository.revokeAllByUser(user);

        String tokenHash = tokenCryptoService.sha256(rawRefreshToken);
        return refreshTokenRepository.save(new RefreshToken(tokenHash, user, jti, expiresAt));
    }

    @Transactional
    public RefreshToken getValidTokenForUpdate(String rawRefreshToken) {
        String tokenHash = tokenCryptoService.sha256(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RevokedTokenException("refresh token revoked");
        }

        if (refreshToken.isExpired()) {
            throw new ExpiredTokenException("refresh token expired");
        }

        return refreshToken;
    }
}