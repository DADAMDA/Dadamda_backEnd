package com.example.Oauth.auth.service;

import com.example.Oauth.auth.exception.InvalidTokenException;
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
    private final JwtDecoder decoder;
    private final String issuer;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).build();
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenMinutes * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(exp)
                .claim("type", "access")
                .claim("provider", user.getProvider())
                .claim("providerSubject", user.getSubject())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .build();

        return encode(claims);
    }

    public RefreshTokenResult issueRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTokenDays * 24 * 60 * 60);
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(exp)
                .id(jti)
                .claim("type", "refresh")
                .build();

        return new RefreshTokenResult(encode(claims), jti, exp);
    }

    public RefreshTokenClaims parseAndValidateRefreshToken(String refreshToken) {
        try {
            Jwt jwt = decoder.decode(refreshToken);

            String type = jwt.getClaimAsString("type");
            if (!"refresh".equals(type)) {
                throw new InvalidTokenException("not a refresh token");
            }

            return new RefreshTokenClaims(
                    jwt.getSubject(),
                    jwt.getId(),
                    jwt.getExpiresAt()
            );
        } catch (JwtException e) {
            throw new InvalidTokenException("invalid refresh token");
        }
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public record RefreshTokenResult(String token, String jti, Instant expiresAt) {}
    public record RefreshTokenClaims(String userId, String jti, Instant expiresAt) {}
}