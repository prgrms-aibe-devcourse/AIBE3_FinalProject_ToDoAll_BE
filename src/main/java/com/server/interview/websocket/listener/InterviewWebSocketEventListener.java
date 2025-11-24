package com.server.interview.websocket.listener;

import com.server.global.config.security.jwt.JwtAuthentication;
import com.server.interview.websocket.service.InterviewWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class InterviewWebSocketEventListener {

    private final InterviewWebSocketService interviewWebSocketService;

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String value = accessor.getFirstNativeHeader("interviewId");

        Principal principal = accessor.getUser();
        Long userId = null;

        if (principal instanceof JwtAuthentication auth) {
            userId = auth.getUserId();
        }

        boolean isInterviewer = (principal != null);

        if(value != null) {
            Long interviewId = Long.parseLong(value);
            interviewWebSocketService.handleUserJoin(interviewId, userId, sessionId, isInterviewer);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent evnet) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(evnet.getMessage());
        String sessionId = accessor.getSessionId();

        interviewWebSocketService.handleUserLeave(sessionId);
    }
}
