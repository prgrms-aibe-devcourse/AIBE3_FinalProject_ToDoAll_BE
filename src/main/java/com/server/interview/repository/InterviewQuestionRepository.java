package com.server.interview.repository;

import com.server.interview.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    @Modifying
    @Query("DELETE FROM InterviewQuestion q WHERE q.interview.id = :interviewId")
    void deleteByInterviewId(Long interviewId);

}
