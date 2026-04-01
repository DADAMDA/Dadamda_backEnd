package com.example.Oauth.auth.repository;

import com.example.Oauth.auth.token.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    //refresh token row에 대해 한 트랜잭션만 먼저 잠금 처리
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshToken rt join fetch rt.user where rt.token = :token")
    Optional<RefreshToken> findByTokenForUpdate(@Param("token") String token);
}