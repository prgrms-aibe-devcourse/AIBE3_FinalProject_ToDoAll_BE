package com.server.interview.websocket.service;

import com.server.interview.websocket.domain.ChatMessageEntity;
import com.server.interview.websocket.dto.ChatMessage;
import com.server.interview.websocket.registry.SessionRegistry;
import com.server.interview.websocket.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InterviewWebSocketServiceChatTest {

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private InterviewWebSocketService interviewWebSocketService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 채팅_메시지_DB_저장_및_브로드캐스트_성공() {

        Long interviewId = 1L;

        ChatMessage chatMessage = new ChatMessage(
                interviewId,
                10L,
                "홍길동",
                "안녕하세요"
        );


        when(chatMessageRepository.save(any(ChatMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        interviewWebSocketService.broadcastChatMessage(interviewId, chatMessage);


        // DB 저장 검증
        verify(chatMessageRepository, times(1)).save(any(ChatMessageEntity.class));

        // WebSocket 브로드캐스트 검증
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/interview/" + interviewId + "/chat"), any(ChatMessage.class));
    }
}
