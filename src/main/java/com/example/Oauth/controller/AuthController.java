package com.example.Oauth.controller;

import com.example.Oauth.auth.*;
import com.example.Oauth.user.User;
import com.example.Oauth.user.UserRepository;
import com.example.Oauth.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2/authorization")
public class AuthController {

    private final KakaoApiClient kakaoApiClient;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static void requireToken(TokenRequest req) {
        if (req == null || req.token() == null || req.token().isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
    }

    public AuthController(KakaoApiClient kakaoApiClient,
                          GoogleTokenVerifier googleTokenVerifier,
                          UserService userService,
                          JwtService jwtService, UserRepository userRepository) {
        this.kakaoApiClient = kakaoApiClient;
        this.googleTokenVerifier = googleTokenVerifier;
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * RN -> 서버
     * - Kakao: access_token 전달
     * - Google: id_token 전달
     */
    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakao(@RequestBody TokenRequest req) {
        requireToken(req);

        var info = kakaoApiClient.fetchUserInfo(req.token());

        boolean existed = userRepository.findByProviderAndSubject("kakao", info.id()).isPresent();

        User user = userService.upsert(
                "kakao",
                info.id(),
                info.email(),
                info.nickname(),
                info.profileImageUrl()
        );

        String ourJwt = jwtService.issueAccessToken(user);

        return ResponseEntity.ok(new TokenResponse(ourJwt, !existed));
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> google(@RequestBody TokenRequest req) {

        requireToken(req);

        var info = googleTokenVerifier.verify(req.token()); // id_token 검증

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

        return ResponseEntity.ok(new TokenResponse(ourJwt, !existed));
    }
}
