package com.server.interview.websocket.security;

import com.server.global.config.security.jwt.JwtAuthentication;
import com.server.global.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if(token == null || token.isBlank() || !token.startsWith("Bearer ")) {
                log.info("지원자 접속");
                accessor.setUser(new AnonymousPrincipal());
                return message;
            }

            try {
                token = token.substring(7);
                Long userId = jwtTokenProvider.getUserId(token);
                accessor.setUser(new JwtAuthentication(userId));
            } catch (Exception e) {
                accessor.setUser(new AnonymousPrincipal());
            }
        }

        if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if(destination != null && destination.contains("/note")) {
                Principal principal = accessor.getUser();
                boolean isInterviewer = principal instanceof JwtAuthentication;

                if(!isInterviewer) {
                    log.warn("지원자의 Note Subscribe 차단: destination: " + destination);
                    throw new IllegalArgumentException("NOTE 권한 없음");
                }
            }
        }

        return message;
    }
}
