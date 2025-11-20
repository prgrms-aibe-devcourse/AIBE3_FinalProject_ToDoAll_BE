package com.server.interview.websocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "note_message")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long interviewId;

    private Long senderId;

    private String sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Long noteId;

    private LocalDateTime createdAt;
}
