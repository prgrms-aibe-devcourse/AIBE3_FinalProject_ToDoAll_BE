package com.server.interview.controller;

import com.server.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;


    // 예시 메시지 핸들러 (실제 구현할때 지워주세요.)
    @MessageMapping("/interview/message")
    @SendTo("/topic/interview/room/{roomId}")
    public String handleInterviewMessage(String message) {
        return "[면접 메시지] " + message;
    }
}
