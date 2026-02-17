package com.example.Oauth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/")
    public String home() {
        return """
               OK.
               POST /oauth2/authorization/kakao  { "token": "<kakao_access_token>" }
               POST /oauth2/authorization/google { "token": "<google_id_token>" }
               Then call GET /api/me with Authorization: Bearer <our_jwt>
               """;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("userId", jwt.getSubject());
        body.put("provider", jwt.getClaimAsString("provider"));
        body.put("providerSubject", jwt.getClaimAsString("providerSubject"));
        body.put("email", jwt.getClaimAsString("email")); // null이어도 OK
        body.put("name", jwt.getClaimAsString("name"));   // null이어도 OK
        return body;
    }
}
