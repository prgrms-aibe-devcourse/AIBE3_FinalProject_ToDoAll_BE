package com.server.interview.websocket.security;

import com.server.global.config.security.jwt.JwtAuthentication;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.websocket.registry.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final InterviewParticipantRepository participantRepository;
    private final SessionRegistry sessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String raw = accessor.getFirstNativeHeader("Authorization");
            String token = extractToken(raw);

            if (token != null) {
                try {
                    Long userId = jwtTokenProvider.getUserId(token);

                    // ✅ Principal 설정
                    accessor.setUser(new JwtAuthentication(userId));

                    // ✅ sessionAttributes에 userId 저장 (SUBSCRIBE에서 쓰려고)
                    Map<String, Object> attrs = accessor.getSessionAttributes();
                    if (attrs != null) attrs.put("userId", userId);

                    log.info("WS CONNECT - userId={}", userId);
                } catch (Exception e) {
                    log.warn("JWT 파싱 실패 → 익명 처리", e);
                    accessor.setUser(new AnonymousPrincipal());
                }
            } else {
                accessor.setUser(new AnonymousPrincipal());
                log.info("WS CONNECT - anonymous");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Long interviewId = extractInterviewIdFromDestination(destination);

            // ✅ CONNECT 때 sessionAttributes에 넣어둔 userId를 꺼냄
            Long userId = null;
            Map<String, Object> attrs = accessor.getSessionAttributes();
            if (attrs != null && attrs.get("userId") != null) {
                Object idObj = attrs.get("userId");
                if (idObj instanceof Long) userId = (Long) idObj;
                else if (idObj instanceof String) userId = Long.valueOf((String) idObj);
            }

            // NOTE는 면접관만
            if (destination.contains("/note")) {
                boolean isInterviewer = sessionRegistry.isInterviewer(accessor.getSessionId());
                if (userId == null) {
                    log.warn("NOTE 구독 차단 - 로그인 사용자 아님");
                    throw new IllegalArgumentException("NOTE 구독은 로그인 사용자만 가능합니다.");
                }
                if (!isInterviewer) {
                    log.warn("NOTE 구독 차단 - 면접관 아님");
                    throw new IllegalArgumentException("NOTE 권한 없음");
                }
            }

            // chat/system은 참가자만 (로그인한 경우만 체크)
            if (destination.contains("/chat") || destination.contains("/system")) {
                if (userId != null) {
                    boolean isParticipant =
                            participantRepository.existsByInterviewIdAndUserId(interviewId, userId);
                    if (!isParticipant) {
                        throw new IllegalArgumentException("인터뷰 참가자가 아닙니다.");
                    }
                }
            }
        }

        return message;
    }

    private String extractToken(String raw) {
        if (raw == null) return null;
        if (raw.startsWith("Bearer ")) return raw.substring(7);
        return raw;
    }

    private Long extractInterviewIdFromDestination(String destination) {
        try {
            String[] parts = destination.split("/");
            return Long.parseLong(parts[3]);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 destination: " + destination, e);
        }
    }
}
