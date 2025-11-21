package com.server.interview.websocket.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.websocket.dto.ChatMessageResponseDto;
import com.server.interview.websocket.service.InterviewHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interviews")
public class InterviewHistoryController {
    private final InterviewHistoryService interviewHistoryService;

    @GetMapping("/{interviewId}/chat" )
    public CommonResponse<List<ChatMessageResponseDto>> getChatHistory (
            @PathVariable Long interviewId
    ){
        List<ChatMessageResponseDto> result = interviewHistoryService.getChatHistory(interviewId);
        return CommonResponse.success(result);
    }

}
