package com.server.auth.repository;

import com.server.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findTopByEmailAndCreatedAtAfterOrderByCreatedAtDesc(
            String email,
            LocalDateTime after
    );
    // 특정 이메일에 대해 이미 인증 완료된(verified = true) 토큰이 있는지 여부만 확인
    boolean existsByEmailAndVerifiedAtIsNotNull(String email);
}

