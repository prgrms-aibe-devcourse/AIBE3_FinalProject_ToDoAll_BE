package com.server.interview.repository;

import com.server.interview.domain.InterviewEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluation, Long> {
    boolean existsByInterviewId(Long interviewId);

    @Modifying
    @Query("DELETE FROM InterviewEvaluation e WHERE e.interview.id = :interviewId")
    void deleteByInterviewId(Long interviewId);

    Optional<InterviewEvaluation> findByInterviewId(Long interviewId);
}
