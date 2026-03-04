package com.example.Oauth.auth.verifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoogleTokenVerifier {

    private final JwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String googleClientId) {
        NimbusJwtDecoder base = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation("https://accounts.google.com");

        OAuth2TokenValidator<Jwt> issuer = token -> {
            String iss = token.getIssuer() != null ? token.getIssuer().toString() : null;
            if ("https://accounts.google.com".equals(iss) || "accounts.google.com".equals(iss)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid issuer: " + iss, null)
            );
        };

        OAuth2TokenValidator<Jwt> audience = new AudienceValidator(googleClientId);

        // (권장) exp/nbf 등 기본 검증도 포함하고 싶으면 JwtValidators.createDefaultWithIssuer(...) 조합 가능
        base.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuer, audience));

        this.decoder = base;
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

        // ✅ 필수값 강제: 없으면 거절
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
            throw new IllegalArgumentException("google profile image is required");
        }

        return new GoogleUserInfo(sub, email, name, picture);
    }

    public record GoogleUserInfo(String sub, String email, String name, String pictureUrl) {}

    static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String requiredAud;

        AudienceValidator(String requiredAud) {
            this.requiredAud = requiredAud;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            List<String> aud = token.getAudience();
            if (aud != null && aud.contains(requiredAud)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error("invalid_token", "Invalid audience", null);
            return OAuth2TokenValidatorResult.failure(err);
        }
    }
}
