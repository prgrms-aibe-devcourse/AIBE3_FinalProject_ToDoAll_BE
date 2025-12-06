package com.server.resume.repository;

import com.server.resume.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Query("""
        select r from Resume r
        left join fetch r.jobDescription jd
        where r.id = :id
        """)
    Optional<Resume> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.jobDescription
        LEFT JOIN FETCH r.educations 
        WHERE r.id = :resumeId
        """)
    Optional<Resume> findWithEssentialDetailsById(@Param("resumeId") Long resumeId);

    @Query("""
        select distinct r from Resume r
        left join fetch r.jobDescription jd
        where r.deletedAt is null
        order by r.createdAt desc
        """)
    List<Resume> findAllWithJobDescriptionOrderByCreatedAtDesc();

    Long countByJobDescriptionId(Long jobDescriptionId);
}