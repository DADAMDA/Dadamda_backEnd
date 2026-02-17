package com.example.Oauth.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_subject", columnNames = {"provider", "subject"}))
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String provider; // google, kakao

    @Column(nullable = false, length = 200)
    private String subject;  // google: sub, kakao: id(문자열화)

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String profileImageUrl;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected User() {}

    public User(String provider, String subject, String email, String name, String profileImageUrl) {
        this.provider = provider;
        this.subject = subject;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public String getSubject() { return subject; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getProfileImageUrl() { return profileImageUrl; }

    public void updateProfile(String email, String name, String profileImageUrl) {
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = Instant.now();
    }
}
