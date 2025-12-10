package com.server.interview.websocket.service;

import com.server.interview.dto.InterviewNoteMemoCreateRequestDto;
import com.server.interview.dto.InterviewNoteMemoCreateResponseDto;
import com.server.interview.service.InterviewNoteMemoService;
import com.server.interview.websocket.dto.NoteMessage;
import com.server.interview.websocket.dto.NoteMessageRequestDto;
import com.server.interview.websocket.registry.SessionRegistry;
import com.server.user.dto.UserProfileResponseDto;
import com.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InterviewWebSocketServiceNoteTest {

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private InterviewNoteMemoService interviewNoteMemoService;

    @Mock
    private UserService userService;

    @InjectMocks
    private InterviewWebSocketService interviewWebSocketService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void NOTE_메시지_면접관일_때_DB저장_및_브로드캐스트_성공() {

        Long interviewId = 1L;
        String sessionId   = "session123";

        NoteMessageRequestDto requestDto = new NoteMessageRequestDto(
                "지원자의 커뮤니케이션 능력 좋음"
        );

        when(sessionRegistry.isInterviewer(sessionId)).thenReturn(true);
        when(sessionRegistry.getUserIdBySession(sessionId)).thenReturn(10L);

        UserProfileResponseDto profile = new UserProfileResponseDto(
                10L, "mail@test.com", "면접관A", "nick",
                "company", "position",
                "010-1234-5678", LocalDate.now(), null,null
        );
        when(userService.getMyProfile(10L)).thenReturn(profile);

        when(interviewNoteMemoService.createByUser(
                eq(interviewId),
                eq(10L),
                any(InterviewNoteMemoCreateRequestDto.class)
        )).thenReturn(new InterviewNoteMemoCreateResponseDto(99L));

        // WHEN
        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, requestDto);

        // THEN — DB 저장 호출됨
        verify(interviewNoteMemoService, times(1))
                .createByUser(eq(interviewId), eq(10L), any(InterviewNoteMemoCreateRequestDto.class));

        // THEN — WebSocket broadcast 호출됨
        verify(messagingTemplate, times(1))
                .convertAndSend(
                        eq("/topic/interview/" + interviewId + "/note"),
                        any(NoteMessage.class)
                );
    }

    @Test
    void NOTE_메시지_면접관아닐때_DB저장_X_브로드캐스트_X() {

        Long interviewId = 1L;
        String sessionId = "session_not_interviewer";

        NoteMessageRequestDto requestDto = new NoteMessageRequestDto(
                "이 메모는 보내지면 안 됨"
        );

        when(sessionRegistry.isInterviewer(sessionId)).thenReturn(false);

        // WHEN
        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, requestDto);

        // THEN — DB 저장 안 됨
        verify(interviewNoteMemoService, never())
                .create(anyLong(), any());

        // THEN — WebSocket broadcast 안 됨
        verify(messagingTemplate, never())
                .convertAndSend(anyString(), nullable(Object.class));

    }

}
