package com.example.Oauth.service.social;

import org.springframework.stereotype.Component;

@Component
public class AppleOauth implements SocialOauth {
    @Override
    public String getOauthRedirectURL() {
        return "";
    }

    @Override
    public String requestAccessToken(String code) {
        return null;
    }
}