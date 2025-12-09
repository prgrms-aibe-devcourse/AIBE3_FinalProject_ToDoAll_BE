package com.server.interview.websocket.service;

import com.server.interview.service.InterviewNoteMemoService;
import com.server.interview.websocket.dto.NoteMessageRequestDto;
import com.server.interview.websocket.registry.SessionRegistry;
import com.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

class InterviewWebSocketServiceNoteBlockTest {

    @Mock private SessionRegistry sessionRegistry;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private InterviewNoteMemoService interviewNoteMemoService;
    @Mock private UserService userService;

    @InjectMocks private InterviewWebSocketService interviewWebSocketService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void NOTE_면접관이지만_userId_null이면_DB저장_X_브로드캐스트_X() {
        Long interviewId = 1L;
        String sessionId = "s1";

        when(sessionRegistry.isInterviewer(sessionId)).thenReturn(true);
        when(sessionRegistry.getUserIdBySession(sessionId)).thenReturn(null);

        // WHEN
        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, new NoteMessageRequestDto("memo"));

        // THEN
        verify(interviewNoteMemoService, never()).create(anyLong(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        verify(userService, never()).getMyProfile(anyLong());
    }


    @Test
    void NOTE_면접관이지만_userId_minus1이면_DB저장_X_브로드캐스트_X() {
        Long interviewId = 1L;
        String sessionId = "s1";

        when(sessionRegistry.isInterviewer(sessionId)).thenReturn(true);
        when(sessionRegistry.getUserIdBySession(sessionId)).thenReturn(-1L);

        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, new NoteMessageRequestDto("memo"));

        verify(interviewNoteMemoService, never()).create(anyLong(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        verify(userService, never()).getMyProfile(anyLong());
    }
}
