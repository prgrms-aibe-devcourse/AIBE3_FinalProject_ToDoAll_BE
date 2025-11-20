package com.server.interview.websocket.repository;

import com.server.interview.websocket.domain.NoteMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteMessageRepository extends JpaRepository<NoteMessageEntity, Long> {
}
