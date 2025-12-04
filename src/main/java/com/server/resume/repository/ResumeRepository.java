package com.server.resume.repository;

import com.server.resume.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Query("""
    SELECT r FROM Resume r
    LEFT JOIN FETCH r.jobDescription
    WHERE r.id = :resumeId
""")
    Optional<Resume> findByIdWithDetails(@Param("resumeId") Long resumeId);

    @Query("""
    SELECT DISTINCT r FROM Resume r
    LEFT JOIN FETCH r.jobDescription
    LEFT JOIN FETCH r.educations 
    WHERE r.id = :resumeId
""")
    Optional<Resume> findWithEssentialDetailsById(@Param("resumeId") Long resumeId);

    // 관리자용: 생성일 내림차순 전체 조회
    List<Resume> findAllByOrderByCreatedAtDesc();
}