package com.example.Oauth.auth.verifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class GoogleTokenVerifier {

    private final JwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String googleClientId) {
        NimbusJwtDecoder jwtDecoder =
                (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation("https://accounts.google.com");

        OAuth2TokenValidator<Jwt> issuerValidator = token -> {
            String iss = token.getIssuer() != null ? token.getIssuer().toString() : null;
            if ("https://accounts.google.com".equals(iss) || "accounts.google.com".equals(iss)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid issuer", null)
            );
        };

        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
            List<String> aud = token.getAudience();
            if (aud != null && aud.contains(googleClientId)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid audience", null)
            );
        };
        // Google ID TOKEN 시간 추가
        JwtTimestampValidator timestampValidator =
                new JwtTimestampValidator(Duration.ofSeconds(60));

        jwtDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator,
                        timestampValidator
                )
        );

        this.decoder = jwtDecoder;
    }

    public GoogleUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("google id_token is required");
        }

        Jwt jwt = decoder.decode(idToken);

        String sub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String picture = jwt.getClaimAsString("picture");

        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("google sub is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("google email is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("google name is required");
        }
        if (picture == null || picture.isBlank()) {
            throw new IllegalArgumentException("google picture is required");
        }

        return new GoogleUserInfo(sub, email, name, picture);
    }

    public record GoogleUserInfo(String sub, String email, String name, String pictureUrl) {}
}
