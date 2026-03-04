package com.example.Oauth.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class KakaoApiClient {

    private final RestClient restClient;
    private final String userInfoUrl;

    public KakaoApiClient(@Value("${app.kakao.userinfo-url}") String userInfoUrl) {
        if (userInfoUrl == null || userInfoUrl.isBlank()) {
            throw new IllegalArgumentException("app.kakao.userinfo-url is required");
        }
        this.restClient = RestClient.create();
        this.userInfoUrl = userInfoUrl;
    }

    @SuppressWarnings("unchecked")
    public KakaoUserInfo fetchUserInfo(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("kakao access token is required");
        }

        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri(userInfoUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException e) {
            // 외부 API 실패는 토큰 문제로 간주 (401 등)
            throw new IllegalArgumentException("kakao user/me failed: HTTP " + e.getRawStatusCode());
        }

        if (body == null || body.get("id") == null) {
            throw new IllegalArgumentException("Invalid Kakao user info response (missing id)");
        }

        String kakaoId = String.valueOf(body.get("id"));

        String nickname = null;
        String profileImageUrl = null;
        String email = null;

        Object kakaoAccountObj = body.get("kakao_account");
        if (kakaoAccountObj instanceof Map<?, ?> kakaoAccount) {

            // profile
            Object profileObj = kakaoAccount.get("profile");
            if (profileObj instanceof Map<?, ?> profile) {
                Object nn = profile.get("nickname");
                if (nn != null) nickname = String.valueOf(nn);

                Object pi = profile.get("profile_image_url");
                if (pi == null) pi = profile.get("thumbnail_image_url"); // fallback
                if (pi != null) profileImageUrl = String.valueOf(pi);
            }

            // email
            Object em = kakaoAccount.get("email");
            if (em != null) email = String.valueOf(em);

            // (선택) has_email 체크까지 하고 싶으면:
            // Object hasEmail = kakaoAccount.get("has_email");
            // if (hasEmail instanceof Boolean b && !b) email = null;
        }

        // ✅ 필수값 강제: 없으면 로그인 거절
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("kakao email is required (consent: account_email).");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("kakao nickname is required (consent: profile_nickname).");
        }
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            throw new IllegalArgumentException("kakao profile image is required (consent: profile_image).");
        }

        return new KakaoUserInfo(kakaoId, email, nickname, profileImageUrl);
    }

    public record KakaoUserInfo(String id, String email, String nickname, String profileImageUrl) {}
}
