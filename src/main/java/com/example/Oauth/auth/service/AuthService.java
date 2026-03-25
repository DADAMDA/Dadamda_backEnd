package com.example.Oauth.auth.service;

import com.example.Oauth.auth.client.KakaoApiClient;
import com.example.Oauth.auth.verifier.GoogleTokenVerifier;
import com.example.Oauth.auth.token.TokenResponse;
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
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthService(KakaoApiClient kakaoApiClient,
                       GoogleTokenVerifier googleTokenVerifier,
                       UserService userService,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       UserRepository userRepository) {
        this.kakaoApiClient = kakaoApiClient;
        this.googleTokenVerifier = googleTokenVerifier;
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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

        String accessToken = jwtService.issueAccessToken(user);
        String refreshToken = jwtService.issueRefreshToken(user);
        refreshTokenService.save(user, refreshToken);

        return new TokenResponse(accessToken, refreshToken, !existed);
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

        String accessToken = jwtService.issueAccessToken(user);
        String refreshToken = jwtService.issueRefreshToken(user);
        refreshTokenService.save(user, refreshToken);

        return new TokenResponse(accessToken, refreshToken, !existed);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        var savedToken = refreshTokenService.getValidToken(refreshToken);
        User user = savedToken.getUser();

        savedToken.revoke();

        String newAccessToken = jwtService.issueAccessToken(user);
        String newRefreshToken = jwtService.issueRefreshToken(user);
        refreshTokenService.save(user, newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken, false);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}