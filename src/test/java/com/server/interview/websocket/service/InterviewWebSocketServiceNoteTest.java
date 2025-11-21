package com.server.interview.websocket.service;

import com.server.interview.websocket.dto.NoteMessage;
import com.server.interview.service.InterviewNoteMemoService;
import com.server.interview.dto.InterviewNoteMemoCreateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InterviewWebSocketServiceNoteTest {

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private InterviewNoteMemoService interviewNoteMemoService;

    @InjectMocks
    private InterviewWebSocketService interviewWebSocketService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void NOTE_메시지_면접관일_때_DB저장_및_브로드캐스트_성공() {

        Long interviewId = 1L;
        String sessionId = "session123";

        NoteMessage noteMessage = new NoteMessage(
                interviewId,
                10L,
                "면접관A",
                "지원자의 커뮤니케이션 능력 좋음",
                99L
        );

        // 면접관임(true)
        when(sessionRegistry.isInterviewer(sessionId)).thenReturn(true);

        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, noteMessage);

        // DB 저장 수행
        verify(interviewNoteMemoService, times(1))
                .create(eq(interviewId), any(InterviewNoteMemoCreateRequestDto.class));

        // WebSocket broadcast 호출
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/interview/" + interviewId + "/note"), eq(noteMessage));
    }


    @Test
    void NOTE_메시지_면접관이_아닐때_DB저장_X_브로드캐스트_X() {
        Long interviewId = 1L;
        String sessionId = "session_not_interviewer";

        NoteMessage noteMessage = new NoteMessage(
                interviewId,
                200L,
                "참관자A",
                "이 메모는 보내지면 안 됨",
                888L
        );

        when(sessionRegistry.isInterviewer(sessionId)).thenReturn(false);


        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, noteMessage);

        // interviewNoteMemoService를 사용하지 않아 DB 저장 안 되어야 함
        verify(interviewNoteMemoService, never())
                .create(anyLong(), any());

        // WebSocket broadcast 호출이 되지 않아야 한다
        verify(messagingTemplate, never())
                .convertAndSend(anyString(), (Object) any());
    }

}
