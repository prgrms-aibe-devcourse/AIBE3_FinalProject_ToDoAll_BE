package com.server.interview.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessage extends InterviewMessage {

    private Long senderId;

    @NotBlank(message = "sender는 null일 수 없습니다.")
    private String sender;

    @NotBlank(message = "content는 비어 있을 수 없습니다.")
    private String content;

    @Builder
    public ChatMessage(Long interviewId, Long senderId, String sender, String content) {
        super(MessageType.CHAT, interviewId);
        this.senderId = senderId;
        this.sender = sender;
        this.content = content;
    }
}
