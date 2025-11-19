package com.server.interview.websocket.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewMessage {
    private MessageType type;      // CHAT, NOTE, CHECK, JOIN, LEAVE, SYSTEM
    private Long interviewId;      // 면접 ID

    private Long senderId;         // 작성자 ID
    private String sender;         // 작성자 이름

    private String content;        // 메시지 내용
    private String event;



    public static InterviewMessage of(Long interviewId, String content, String event) {
        return new InterviewMessage(
                MessageType.SYSTEM,
                interviewId,
                null,
                "SYSTEM",
                content,
                event
        );
    }

    public static InterviewMessage chat(Long interviewId, Long senderId, String sender, String content, String event) {
        return new InterviewMessage(
                MessageType.CHAT,
                interviewId,
                senderId,
                sender,
                content,
                event
        );
    }

    public static InterviewMessage note(Long interviewId, Long senderId, String sender, String content, String event) {
        return new InterviewMessage(
                MessageType.NOTE,
                interviewId,
                senderId,
                sender,
                content,
                event
        );
    }

}
