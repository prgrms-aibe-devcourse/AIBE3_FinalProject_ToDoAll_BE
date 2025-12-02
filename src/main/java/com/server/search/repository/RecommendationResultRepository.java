package com.server.search.repository;

import com.server.search.domain.RecommendationResult;
import com.server.search.domain.RecommendationKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, RecommendationKey> {
    Optional<RecommendationResult> findByJdIdAndResumeId(Long jdId, Long resumeId);
    List<RecommendationResult> findAllByJdId(Long jdId);
}
