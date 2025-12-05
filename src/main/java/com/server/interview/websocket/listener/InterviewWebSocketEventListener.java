package com.server.interview.websocket.listener;

import com.server.interview.websocket.service.InterviewWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewWebSocketEventListener {

    private final InterviewWebSocketService interviewWebSocketService;

    // 연결할 때의 이벤트 처리
    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String interviewIdHeader = accessor.getFirstNativeHeader("interviewId");

        if (interviewIdHeader == null) {
            log.warn("interviewId 헤더 없음 - 연결 차단");
            return;
        }

        Long interviewId = Long.parseLong(interviewIdHeader);


        // userId 가지고 옴
        Long userId = null;
        if(accessor.getSessionAttributes() != null) {
            Object idObj = accessor.getSessionAttributes().get("userId");
            if(idObj instanceof Long) userId = (Long) idObj;
            else if(idObj instanceof String) userId = Long.valueOf((String)idObj);
        }

        if (userId == null) {
            userId = -1L;
            log.info("INTERVIEW CONNECT: Anonymous 접속 (sessionId={})", sessionId);
        } else {
            log.info("INTERVIEW CONNECT userId={} (sessionId={})", userId, sessionId);
        }

        interviewWebSocketService.handleUserJoin(interviewId, userId, sessionId);
    }


    // 연결을 끊을 때의 이벤트 처리
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        interviewWebSocketService.handleUserLeave(sessionId);
    }
}
