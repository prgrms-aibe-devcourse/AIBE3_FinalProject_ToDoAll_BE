package com.server.interview.websocket.security;

import com.server.global.config.security.jwt.JwtAuthentication;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.websocket.service.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

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

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String raw = accessor.getFirstNativeHeader("Authorization");
            String interviewIdHeader = accessor.getFirstNativeHeader("interviewId");

            if(interviewIdHeader == null) {
                throw new IllegalArgumentException("interviewId 헤더가 필요합니다.");
            }

            Long interviewId = Long.parseLong(interviewIdHeader);

            String token = extractToken(raw);

            if(token != null) {
                try {
                    Long userId = jwtTokenProvider.getUserId(token);

                    boolean isParticipant = participantRepository.existsByInterviewIdAndUserId(interviewId, userId);

                    if(!isParticipant) {
                        log.warn("로그인 사용자가 participant가 아니므로 차단되었습니다.");
                        throw new IllegalArgumentException("해당 인터뷰의 참여자가 아닙니다.");
                    }

                    accessor.setUser(new JwtAuthentication(userId));
                    log.info("INTERVIEW CONNECT");
                } catch (Exception e) {
                    log.warn("JWT 파싱 실패 → 익명 처리", e);
                    accessor.setUser(new AnonymousPrincipal());
                }
            } else {
                accessor.setUser(new AnonymousPrincipal());
                log.info("INTERVIEW CONNECT: anonymous");
            }
        }

        if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if(destination != null && destination.contains("/note")) {
                boolean isInterviewer = sessionRegistry.isInterviewer(accessor.getSessionId());


                if(!isInterviewer) {
                    log.warn("지원자의 Note Subscribe 차단: destination: " + destination);
                    throw new IllegalArgumentException("NOTE 권한 없음");
                }
            }
        }

        return message;
    }

    private String extractToken(String raw) {
        if(raw == null) return null;

        if(raw.startsWith("Bearer ")) {
            return raw.substring(7);
        }

        return raw;
    }
}
