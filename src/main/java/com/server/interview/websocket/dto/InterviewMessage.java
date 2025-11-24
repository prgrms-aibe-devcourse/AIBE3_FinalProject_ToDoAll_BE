package com.server.interview.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class InterviewMessage {
    protected MessageType type;
    protected Long interviewId;

    protected InterviewMessage(MessageType type, Long interviewId) {
        this.type = type;
        this.interviewId = interviewId;
    }
}
