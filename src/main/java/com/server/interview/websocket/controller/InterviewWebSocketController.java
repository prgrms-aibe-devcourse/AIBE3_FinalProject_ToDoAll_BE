package com.server.interview.websocket.controller;

import com.server.interview.websocket.dto.InterviewMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InterviewWebSocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/interview/{interviewId}/chat")
    public void handleChatMessage(@DestinationVariable Long interviewId, InterviewMessage message) {
        simpMessagingTemplate.convertAndSend("/topic/interview/" + interviewId + "/chat", message);
    }

    @MessageMapping("/interview/{interviewId}/note")
    public void handleNoteMessage(@DestinationVariable Long interviewId, InterviewMessage message) {
        simpMessagingTemplate.convertAndSend("/topic/interview/" + interviewId + "/note", message);
    }

    @MessageMapping("/interview/{interviewId}/system")
    public void handleSystemMessage(@DestinationVariable Long interviewId, InterviewMessage message) {
        simpMessagingTemplate.convertAndSend("/topic/interview/" + interviewId + "/system", message);
    }
}
