package com.server.interview.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessage extends InterviewMessage {

    private Long senderId;
    private String sender;
    private String content;

    public ChatMessage(Long interviewId, Long senderId, String sender, String content) {
        super(MessageType.CHAT, interviewId);
        this.senderId = senderId;
        this.sender = sender;
        this.content = content;
    }
}
