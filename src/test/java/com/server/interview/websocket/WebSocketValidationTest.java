package com.server.interview.websocket;

import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.websocket.dto.ChatMessage;
import com.server.interview.websocket.registry.SessionRegistry;
import com.server.user.domain.Gender;
import com.server.user.dto.UserProfileResponseDto;
import com.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketValidationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @MockitoBean
    private SessionRegistry sessionRegistry;

    @MockitoBean
    private InterviewParticipantRepository interviewParticipantRepository;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        when(interviewParticipantRepository.existsByInterviewIdAndUserId(anyLong(), anyLong()))
                .thenReturn(true);

        when(sessionRegistry.isInterviewer(anyString())).thenReturn(true);
        when(sessionRegistry.getUserIdBySession(anyString())).thenReturn(100L);

        when(userService.getMyProfile(anyLong()))
                .thenReturn(new UserProfileResponseDto(
                        100L,
                        "tester@example.com",
                        "tester",
                        "nickname",
                        "TestCompany",
                        "Developer",
                        "010-0000-0000",
                        LocalDate.of(1990, 1, 1),
                        Gender.MALE));

    }

    private StompSession connect() throws Exception {

        List<Transport> transports =
                List.of(new WebSocketTransport(new StandardWebSocketClient()));

        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
        StompHeaders stompHeaders = new StompHeaders();


        stompHeaders.add("interviewId", "1");


        return stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/ws/interview",
                        wsHeaders,
                        stompHeaders,
                        new StompSessionHandlerAdapter() {})
                .get(2, TimeUnit.SECONDS);

    }

    @Test
    void invalidMessage_shouldNotBeBroadcast() throws Exception {
        StompSession session = connect();

        CompletableFuture<ChatMessage> future = new CompletableFuture<>();

        session.subscribe("/topic/interview/1/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((ChatMessage) payload);
            }
        });

        ChatMessage invalid = ChatMessage.builder()
                .senderId(1L)
                .sender("tester")
                .content("")
                .build();

        session.send("/app/interview/1/chat", invalid);

        assertThatThrownBy(() -> future.get(500, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void validMessage_shouldBeBroadcast() throws Exception {
        StompSession session = connect();

        CompletableFuture<ChatMessage> future = new CompletableFuture<>();

        session.subscribe("/topic/interview/1/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((ChatMessage) payload);
            }
        });

        ChatMessage valid = ChatMessage.builder()
                .senderId(1L)
                .sender("tester")
                .content("hello world")
                .build();

        session.send("/app/interview/1/chat", valid);

        ChatMessage received = future.get(2, TimeUnit.SECONDS);

        assertEquals("hello world", received.getContent());
        assertEquals(1L, received.getSenderId());
        assertEquals("tester", received.getSender());
    }
}
