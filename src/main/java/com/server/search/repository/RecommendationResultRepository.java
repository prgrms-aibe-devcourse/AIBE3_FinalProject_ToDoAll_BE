package com.server.search.repository;

import com.server.search.domain.RecommendationResult;
import com.server.search.domain.RecommendationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, RecommendationKey> {
    Optional<RecommendationResult> findByJdIdAndResumeId(Long jdId, Long resumeId);
    List<RecommendationResult> findAllByJdId(Long jdId);

    @Query("SELECT r.resumeId FROM RecommendationResult r WHERE r.jdId = :jdId")
    List<Long> findResumeIdsByJdId(@Param("jdId") Long jdId);
}
