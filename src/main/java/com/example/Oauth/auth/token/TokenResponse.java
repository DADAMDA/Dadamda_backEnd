package com.example.Oauth.auth.token;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean isNew
) {}