package com.server.interview.repository;

import com.server.interview.domain.InterviewParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipant, Long> {
}
