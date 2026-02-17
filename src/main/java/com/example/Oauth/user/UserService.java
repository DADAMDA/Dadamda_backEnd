package com.example.Oauth.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public User upsert(String provider, String subject, String email, String name, String profileImageUrl) {
        return repo.findByProviderAndSubject(provider, subject)
                .map(u -> {
                    u.updateProfile(email, name, profileImageUrl);
                    return u;
                })
                .orElseGet(() -> repo.save(new User(provider, subject, email, name, profileImageUrl)));
    }
}