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

}