package com.server.interview.websocket.listener;

import com.server.global.config.security.jwt.JwtAuthentication;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.interview.repository.InterviewParticipantRepository;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final InterviewParticipantRepository interviewParticipantRepository;

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String interviewIdHeader = accessor.getFirstNativeHeader("interviewId");
        String raw = accessor.getFirstNativeHeader("Authorization");

        Long userId = -1L;
        boolean isInterviewer = false;

        if (interviewIdHeader == null) {
            log.warn("interviewId 헤더 없음 - 연결 차단");
            return;
        }

        Long interviewId = Long.parseLong(interviewIdHeader);
        String token = extractToken(raw);

        if (token != null) {
            try {
                userId = jwtTokenProvider.getUserId(token);

                isInterviewer = interviewParticipantRepository.existsByInterviewIdAndUserId(interviewId, userId);

                log.info("INTERVIEW CONNECT userId={}, interviewer={}", userId, isInterviewer);

                accessor.setUser(new JwtAuthentication(userId));
            } catch (Exception e) {
                log.warn("JWT 파싱 실패 → 익명 처리", e);
            }
        } else {
            log.info("INTERVIEW CONNECT: Anonymous 접속");
        }

        interviewWebSocketService.handleUserJoin(interviewId, userId, sessionId, isInterviewer);
    }

    private String extractToken(String raw) {
        if (raw == null) return null;

        if (raw.startsWith("Bearer ")) {
            return raw.substring(7);
        }

        return raw;
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        interviewWebSocketService.handleUserLeave(sessionId);
    }
}
