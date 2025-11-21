package com.server.interview.websocket.repository;

import com.server.interview.websocket.domain.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByInterviewIdOrderByCreatedAtAsc(Long interviewId);
}