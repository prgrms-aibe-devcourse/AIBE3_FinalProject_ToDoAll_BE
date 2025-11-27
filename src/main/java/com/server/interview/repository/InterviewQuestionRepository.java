package com.server.interview.repository;

import com.server.interview.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    @Modifying
    @Query("DELETE FROM InterviewQuestion q WHERE q.interview.id = :interviewId")
    void deleteByInterviewId(@Param("interviewId") Long interviewId);

    @Modifying
    @Query("DELETE FROM InterviewQuestion q WHERE q.id IN :ids AND q.interview.id = :interviewId")
    int deleteByIdsAndInterviewId(@Param("ids") List<Long> ids, @Param("interviewId") Long interviewId);

    List<InterviewQuestion> findAllByInterviewId(Long interviewId);
}
