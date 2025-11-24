package com.server.interview.websocket.dto;

import com.server.interview.websocket.domain.ChatMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private Long senderId;
    private String sender;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponseDto from(ChatMessageEntity entity) {
        return new ChatMessageResponseDto(
                entity.getId(),
                entity.getSenderId(),
                entity.getSender(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
