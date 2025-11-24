//package com.server.interview.websocket;
//
//import com.server.interview.websocket.dto.ChatMessage;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.stomp.StompFrameHandler;
//import org.springframework.messaging.simp.stomp.StompHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//import org.springframework.web.socket.sockjs.client.SockJsClient;
//import org.springframework.web.socket.sockjs.client.Transport;
//import org.springframework.web.socket.sockjs.client.WebSocketTransport;
//
//import java.lang.reflect.Type;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class WebSocketValidationTest {
//
//    @LocalServerPort
//    private int port;
//
//    private WebSocketStompClient stompClient;
//
//    @BeforeEach
//    void setup() {
//        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//    }
//
//    private StompSession connect() throws Exception {
//
//        List<Transport> transports =
//                List.of(new WebSocketTransport(new StandardWebSocketClient()));
//
//        SockJsClient sockJsClient = new SockJsClient(transports);
//        stompClient = new WebSocketStompClient(sockJsClient); // ✔ 기존 stompClient 재사용
//
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
//        return stompClient.connectAsync(
//                "ws://localhost:" + port + "/ws/interview",
//                new StompSessionHandlerAdapter() {}
//        ).get(2, TimeUnit.SECONDS);
//    }
//
//
//    @Test
//    void invalidMessage_shouldNotBeBroadcast() throws Exception {
//        StompSession session = connect();
//
//        CompletableFuture<ChatMessage> future = new CompletableFuture<>();
//
//        session.subscribe("/topic/interview/1/chat", new StompFrameHandler() {
//            @Override
//            public Type getPayloadType(StompHeaders headers) {
//                return ChatMessage.class;
//            }
//
//            @Override
//            public void handleFrame(StompHeaders headers, Object payload) {
//                future.complete((ChatMessage) payload);
//            }
//        });
//
//        ChatMessage invalid = ChatMessage.builder()
//                .senderId(1L)
//                .sender("tester")
//                .content("")
//                .build();
//
//        session.send("/app/interview/1/chat", invalid);
//
//        assertThatThrownBy(() -> future.get(500, TimeUnit.MILLISECONDS))
//                .isInstanceOf(TimeoutException.class);
//    }
//
//    @Test
//    void validMessage_shouldBeBroadcast() throws Exception {
//        StompSession session = connect();
//
//        CompletableFuture<ChatMessage> future = new CompletableFuture<>();
//
//        session.subscribe("/topic/interview/1/chat", new StompFrameHandler() {
//            @Override
//            public Type getPayloadType(StompHeaders headers) {
//                return ChatMessage.class;
//            }
//
//            @Override
//            public void handleFrame(StompHeaders headers, Object payload) {
//                future.complete((ChatMessage) payload);
//            }
//        });
//
//        ChatMessage valid = ChatMessage.builder()
//                .senderId(1L)
//                .sender("tester")
//                .content("hello world")
//                .build();
//
//        session.send("/app/interview/1/chat", valid);
//
//        ChatMessage received = future.get(2, TimeUnit.SECONDS);
//
//        assertEquals("hello world", received.getContent());
//        assertEquals(1L, received.getSenderId());
//        assertEquals("tester", received.getSender());
//    }
//}
