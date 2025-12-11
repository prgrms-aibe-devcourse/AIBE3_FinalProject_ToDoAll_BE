package com.server.jd.repository;

import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long>, JobDescriptionRepositoryCustom {

    @Query("SELECT jd FROM JobDescription jd " +
            "LEFT JOIN FETCH jd.requiredSkills " +
            "LEFT JOIN FETCH jd.preferredSkills " +
            "WHERE jd.id = :id")
    Optional<JobDescription> findByIdFetchSkills(@Param("id") Long id);

    long countByStatus(JobStatus status);
    List<JobDescription> findTop5ByOrderByCreatedAtDesc();
    Page<JobDescription> findAllByAuthorId(Long authorId, Pageable pageable);

    List<JobDescription> findByAuthorId(Long userId);
}
