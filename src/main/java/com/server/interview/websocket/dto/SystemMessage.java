package com.server.interview.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SystemMessage extends InterviewMessage {

    private String event;
    private String content;

    public SystemMessage(Long interviewId, String event, String content) {
        super(MessageType.SYSTEM, interviewId);
        this.event = event;
        this.content = content;
    }
}
