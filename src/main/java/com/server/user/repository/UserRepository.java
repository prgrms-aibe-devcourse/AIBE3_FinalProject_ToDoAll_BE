package com.server.user.repository;


import com.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    List<User> findByEmailDomain(String emailDomain);

    long countByDeletedAtIsNull();

    @Query("select count(u) from User u where u.createdAt >= :today and u.deletedAt is null")
    long countJoinedToday(LocalDateTime today);

    List<User> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();
}