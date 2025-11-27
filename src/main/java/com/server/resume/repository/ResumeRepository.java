package com.server.resume.repository;

import com.server.resume.domain.Resume;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Query("""
    SELECT r FROM Resume r
    LEFT JOIN FETCH r.jobDescription
    WHERE r.id = :resumeId
""")
    Optional<Resume> findByIdWithDetails(@Param("resumeId") Long resumeId);

    @Query("""
    SELECT r FROM Resume r
    LEFT JOIN FETCH r.jobDescription
    LEFT JOIN FETCH r.educations
    LEFT JOIN FETCH r.experiences
    LEFT JOIN FETCH r.skills
    LEFT JOIN FETCH r.certifications
    LEFT JOIN FETCH r.activities
    WHERE r.id = :resumeId
""")
    Optional<Resume> findWithDetailsById(@Param("resumeId") Long resumeId);

}