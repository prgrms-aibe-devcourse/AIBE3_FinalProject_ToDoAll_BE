//package com.server.interview.websocket.security;
//
//import com.server.global.config.security.jwt.JwtTokenProvider;
//import com.server.interview.repository.InterviewParticipantRepository;
//import com.server.interview.websocket.registry.SessionRegistry;
//import com.server.user.domain.Gender;
//import com.server.user.dto.UserProfileResponseDto;
//import com.server.user.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//import org.springframework.web.socket.sockjs.client.SockJsClient;
//import org.springframework.web.socket.sockjs.client.WebSocketTransport;
//
//import java.lang.reflect.Type;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//class WebSocketJwtInterceptorTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    @MockitoBean
//    private SessionRegistry sessionRegistry;
//
//    @MockitoBean
//    private InterviewParticipantRepository interviewParticipantRepository;
//
//    @MockitoBean
//    private UserService userService;
//
//
//
//    private WebSocketStompClient stompClient;
//    private String URL;
//
//    @BeforeEach
//    void setup() {
//        URL = "http://localhost:" + port + "/ws/interview";
//
//        stompClient = new WebSocketStompClient(
//                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
//        );
//
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//        stompClient.setDefaultHeartbeat(new long[]{0, 0});
//
//        when(interviewParticipantRepository.existsByInterviewIdAndUserId(anyLong(), anyLong()))
//                .thenReturn(true);
//
//        when(sessionRegistry.isInterviewer(anyString())).thenReturn(true);
//        when(sessionRegistry.getUserIdBySession(anyString())).thenReturn(100L);
//
//        when(userService.getMyProfile(anyLong()))
//                .thenReturn(new UserProfileResponseDto(
//                        100L,
//                        "tester@example.com",
//                        "tester",
//                        "nickname",
//                        "TestCompany",
//                        "Developer",
//                        "010-0000-0000",
//                        LocalDate.of(1990, 1, 1),
//                        Gender.MALE));
//
//
//    }
//
//    @Test
//    void 면접관은_NOTE_구독_가능() throws Exception {
//
//        String accessToken = "Bearer " + jwtTokenProvider.generateToken(100L, "ROLE_USER");
//
//        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
//        StompHeaders stompHeaders = new StompHeaders();
//
//        stompHeaders.add("interviewId", "1");
//        stompHeaders.add("Authorization", accessToken);
//
//        StompSession session = stompClient
//                .connectAsync(URL, wsHeaders, stompHeaders, new StompSessionHandlerAdapter() {})
//                .get();
//
//        session.subscribe("/topic/interview/1/note", new TestHandler());
//    }
//
//    // 🔽🔽🔽 여기에 추가!
//    private static class TestHandler extends StompSessionHandlerAdapter {
//
//        @Override
//        public Type getPayloadType(StompHeaders headers) {
//            return String.class;
//        }
//
//        @Override
//        public void handleFrame(StompHeaders headers, Object payload) {
//            System.out.println("Received: " + payload);
//        }
//
//        @Override
//        public void handleException(StompSession session, StompCommand command,
//                                    StompHeaders headers, byte[] payload, Throwable exception) {
//            throw new RuntimeException(exception);
//        }
//
//        @Override
//        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//            System.out.println("Connected!");
//        }
//    }
//}
