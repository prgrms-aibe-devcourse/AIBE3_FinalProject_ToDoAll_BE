package com.server.auth.repository;

import com.server.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerificationToken> findByToken(String token);
}

