package com.server.interview.websocket.security;

import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.websocket.registry.SessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.*;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class WebSocketJwtInterceptorSubscribeTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private InterviewParticipantRepository participantRepository;
    @Mock private SessionRegistry sessionRegistry;

    private WebSocketJwtInterceptor interceptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        interceptor = new WebSocketJwtInterceptor(jwtTokenProvider, participantRepository, sessionRegistry);
    }

    @Test
    void NOTE_구독은_로그인유저_아니면_차단() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/interview/1/note");
        accessor.setSessionId("s1");
        accessor.setSessionAttributes(new HashMap<>()); 

        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(sessionRegistry.isInterviewer("s1")).thenReturn(true);

        assertThatThrownBy(() -> interceptor.preSend(msg, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOTE 구독은 로그인 사용자만");
    }

    @Test
    void NOTE_구독은_면접관_아니면_차단() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/interview/1/note");
        accessor.setSessionId("s1");

        HashMap<String, Object> attrs = new HashMap<>();
        attrs.put("userId", 10L);
        accessor.setSessionAttributes(attrs);

        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(sessionRegistry.isInterviewer("s1")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(msg, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOTE 권한 없음");
    }

    @Test
    void CHAT_구독은_로그인유저면_참가자아닐때_차단() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/interview/1/chat");
        accessor.setSessionId("s1");

        HashMap<String, Object> attrs = new HashMap<>();
        attrs.put("userId", 10L);
        accessor.setSessionAttributes(attrs);

        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(participantRepository.existsByInterviewIdAndUserId(1L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(msg, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("인터뷰 참가자가 아닙니다.");
    }

    @Test
    void CHAT_구독은_익명_이면_참가자검증을_안하고_통과() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/interview/1/chat");
        accessor.setSessionId("s1");
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(msg, null);

        verify(participantRepository, never()).existsByInterviewIdAndUserId(anyLong(), anyLong());
    }
}
