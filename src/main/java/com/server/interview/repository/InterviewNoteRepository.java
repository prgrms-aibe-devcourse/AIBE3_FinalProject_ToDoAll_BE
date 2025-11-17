package com.server.interview.repository;

import com.server.interview.domain.InterviewNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InterviewNoteRepository extends JpaRepository<InterviewNote, Long> {
    @Modifying
    @Query("DELETE FROM InterviewNote n WHERE n.interview.id = :interviewId")
    void deleteByInterviewId(Long interviewId);

}
