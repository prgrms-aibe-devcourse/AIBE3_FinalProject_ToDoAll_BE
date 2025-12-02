package com.server.search.repository;

import com.server.search.domain.RecommendationResult;
import com.server.search.domain.RecommendationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, RecommendationKey> {
    Optional<RecommendationResult> findByJdIdAndResumeId(Long jdId, Long resumeId);
    List<RecommendationResult> findAllByJdId(Long jdId);

    @Query("SELECT r.resumeId FROM RecommendationResult r WHERE r.jdId = :jdId")
    List<Long> findResumeIdsByJdId(@Param("jdId") Long jdId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM recommendation_missing_skills WHERE jd_id = :jdId", nativeQuery = true)
    void deleteMissingSkillsByJdId(@Param("jdId") Long jdId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM recommendation_results WHERE jd_id = :jdId", nativeQuery = true)
    void deleteByJdId(@Param("jdId") Long jdId);
}
