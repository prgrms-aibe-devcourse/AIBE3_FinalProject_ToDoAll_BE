package com.server.auth.repository;

import com.server.user.domain.PasswordResetToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 비밀번호 재설정 토큰 저장소

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 토큰 해시값으로 재설정 토큰 조회

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    // 만료된 토큰 일괄 삭제 (배치 작업용)

    void deleteByExpiresAtBefore(LocalDateTime now);

    // 특정 사용자의 가장 최근 미사용 토큰 조회

    @Query("""
        SELECT t 
        FROM PasswordResetToken t
        WHERE t.user.id = :userId
          AND t.usedAt IS NULL
          AND t.expiresAt > :now
        ORDER BY t.createdAt DESC
        """)
    Optional<PasswordResetToken> findLatestUnusedToken(Long userId, LocalDateTime now);

}