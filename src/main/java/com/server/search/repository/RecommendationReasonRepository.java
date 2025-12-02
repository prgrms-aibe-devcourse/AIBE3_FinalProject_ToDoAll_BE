package com.server.search.repository;

import com.server.search.domain.RecommendationReason;
import com.server.search.domain.RecommendationKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendationReasonRepository extends JpaRepository<RecommendationReason, RecommendationKey> {
    Optional<RecommendationReason> findByJdIdAndResumeId(Long jdId, Long resumeId);
}