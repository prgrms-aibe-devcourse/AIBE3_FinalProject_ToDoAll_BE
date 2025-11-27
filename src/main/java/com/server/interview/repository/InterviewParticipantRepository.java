package com.server.interview.repository;

import com.server.interview.domain.InterviewParticipant;
import com.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipant, Long> {
    boolean existsByInterviewIdAndUserId(Long interviewId, Long id);

    @Query("select ip.user.id from InterviewParticipant ip where ip.interview.id = :interviewId")
    List<Long> findUserIdsByInterviewId(@Param("interviewId") Long interviewId);

    Long user(User user);
}
