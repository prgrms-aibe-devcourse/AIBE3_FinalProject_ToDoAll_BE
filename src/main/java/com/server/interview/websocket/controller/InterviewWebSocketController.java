package com.server.interview.websocket.controller;

import com.server.interview.websocket.dto.ChatMessage;
import com.server.interview.websocket.dto.NoteMessage;
import com.server.interview.websocket.dto.SystemMessage;
import com.server.interview.websocket.service.InterviewWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InterviewWebSocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final InterviewWebSocketService interviewWebSocketService;

    @MessageMapping("/interview/{interviewId}/chat")
    public void handleChatMessage(@DestinationVariable Long interviewId, ChatMessage message) {
        interviewWebSocketService.broadcastChatMessage(interviewId, message);
    }


    @MessageMapping("/interview/{interviewId}/note")
    public void handleNoteMessage(@DestinationVariable Long interviewId, NoteMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        interviewWebSocketService.broadcastNoteMessage(interviewId, sessionId, message);
    }

    @MessageMapping("/interview/{interviewId}/system")
    public void handleSystemMessage(@DestinationVariable Long interviewId, SystemMessage message) {
        interviewWebSocketService.broadcastSystemMessage(interviewId, message.getEvent(), message.getContent());
    }
}
