package com.example.Oauth.auth.service;

import com.example.Oauth.user.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("app.jwt.secret must not be blank");
        }

        // HS256은 최소 256비트(32바이트) 권장
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes for HS256");
        }

        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("app.jwt.issuer must not be blank");
        }
        this.issuer = issuer;

        if (accessTokenMinutes <= 0) {
            throw new IllegalArgumentException("app.jwt.access-token-minutes must be > 0");
        }
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String issueAccessToken(User user) {
        if (user == null) throw new IllegalArgumentException("user must not be null");
        if (user.getId() == null) throw new IllegalArgumentException("user.id must not be null");
        if (user.getProvider() == null || user.getProvider().isBlank()) {
            throw new IllegalArgumentException("user.provider must not be blank");
        }
        if (user.getSubject() == null || user.getSubject().isBlank()) {
            throw new IllegalArgumentException("user.subject must not be blank");
        }

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenMinutes * 60);

        JwtClaimsSet.Builder b = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(String.valueOf(user.getId()))
                .claim("provider", user.getProvider())
                .claim("providerSubject", user.getSubject());

        // null인 claim은 아예 넣지 않음 (Map.of/JWT 인코딩 null 문제 회피)
        if (user.getName() != null && !user.getName().isBlank()) {
            b.claim("name", user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            b.claim("email", user.getEmail());
        }

        JwtClaimsSet claims = b.build();

        // ✅ HS256 명시 (환경/라이브러리 혼선 방지)
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtEncoderParameters params = JwtEncoderParameters.from(header, claims);

        return encoder.encode(params).getTokenValue();
    }

    public String issueRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTokenDays * 24 * 60 * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(String.valueOf(user.getId()))
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
