package com.example.Oauth.auth.controller;


import com.example.Oauth.auth.service.AuthService;
import com.example.Oauth.dto.auth.TokenRequest;
import com.example.Oauth.dto.auth.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2/authorization")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private static void requireToken(TokenRequest req) {
        if (req == null || req.token() == null || req.token().isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
    }

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakao(@RequestBody TokenRequest req) {
        requireToken(req);
        return ResponseEntity.ok(authService.loginWithKakao(req.token()));
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> google(@RequestBody TokenRequest req) {
        requireToken(req);
        return ResponseEntity.ok(authService.loginWithGoogle(req.token()));
    }
}