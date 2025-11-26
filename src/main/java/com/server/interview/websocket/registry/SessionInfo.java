package com.server.interview.websocket.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionInfo {
    private Long interviewId;
    private Long userId;
    private boolean interviewer;
}
