package com.example.Oauth.auth.controller;

import com.example.Oauth.auth.service.AuthService;
import com.example.Oauth.auth.token.RefreshTokenRequest;
import com.example.Oauth.auth.token.TokenRequest;
import com.example.Oauth.auth.token.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2/authorization")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private static void requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
    }

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakao(@RequestBody TokenRequest req) {
        requireToken(req.token());
        return ResponseEntity.ok(authService.loginWithKakao(req.token()));
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> google(@RequestBody TokenRequest req) {
        requireToken(req.token());
        return ResponseEntity.ok(authService.loginWithGoogle(req.token()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest req) {
        requireToken(req.refreshToken());
        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.ok().build();
    }
}