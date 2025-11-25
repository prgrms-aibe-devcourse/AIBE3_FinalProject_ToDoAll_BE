package com.server.interview.websocket.security;

import com.server.global.config.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketJwtInterceptorTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private WebSocketStompClient stompClient;
    private String URL;

    @BeforeEach
    void setup() {
        URL = "http://localhost:" + port + "/ws/interview";

        stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setDefaultHeartbeat(new long[]{0, 0});
    }

    @Test
    void 면접관은_NOTE_구독_가능() throws Exception {

        String accessToken = "Bearer " + jwtTokenProvider.generateToken(100L, "ROLE_USER");

        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
        StompHeaders stompHeaders = new StompHeaders();

        stompHeaders.add("interviewId", "1");
        stompHeaders.add("Authorization", accessToken);

        StompSession session = stompClient
                .connectAsync(URL, wsHeaders, stompHeaders, new StompSessionHandlerAdapter() {})
                .get();

        session.subscribe("/topic/interview/1/note", new TestHandler());
    }

    // 🔽🔽🔽 여기에 추가!
    private static class TestHandler extends StompSessionHandlerAdapter {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Received: " + payload);
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            throw new RuntimeException(exception);
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected!");
        }
    }
}
