package com.example.Oauth.auth.service;

import com.example.Oauth.auth.client.KakaoApiClient;
import com.example.Oauth.auth.verifier.GoogleTokenVerifier;
import com.example.Oauth.dto.auth.TokenResponse;
import com.example.Oauth.user.User;
import com.example.Oauth.user.UserRepository;
import com.example.Oauth.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final KakaoApiClient kakaoApiClient;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(KakaoApiClient kakaoApiClient,
                       GoogleTokenVerifier googleTokenVerifier,
                       UserService userService,
                       JwtService jwtService,
                       UserRepository userRepository) {
        this.kakaoApiClient = kakaoApiClient;
        this.googleTokenVerifier = googleTokenVerifier;
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Transactional
    public TokenResponse loginWithKakao(String kakaoAccessToken) {
        var info = kakaoApiClient.fetchUserInfo(kakaoAccessToken);

        boolean existed = userRepository
                .findByProviderAndSubject("kakao", info.id())
                .isPresent();

        User user = userService.upsert(
                "kakao",
                info.id(),
                info.email(),
                info.nickname(),
                info.profileImageUrl()
        );

        String ourJwt = jwtService.issueAccessToken(user);
        return new TokenResponse(ourJwt, !existed);
    }

    @Transactional
    public TokenResponse loginWithGoogle(String googleIdToken) {
        var info = googleTokenVerifier.verify(googleIdToken);

        boolean existed = userRepository
                .findByProviderAndSubject("google", info.sub())
                .isPresent();

        User user = userService.upsert(
                "google",
                info.sub(),
                info.email(),
                info.name(),
                info.pictureUrl()
        );

        String ourJwt = jwtService.issueAccessToken(user);
        return new TokenResponse(ourJwt, !existed);
    }
}