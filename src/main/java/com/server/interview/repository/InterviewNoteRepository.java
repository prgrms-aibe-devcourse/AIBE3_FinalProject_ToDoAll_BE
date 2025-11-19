package com.server.interview.repository;

import com.server.interview.domain.InterviewNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewNoteRepository extends JpaRepository<InterviewNote, Long> {
    Optional<InterviewNote> findByInterviewId(Long interviewId);
}
