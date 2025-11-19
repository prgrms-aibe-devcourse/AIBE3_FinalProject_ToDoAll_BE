package com.server.mcp.controller;

import com.server.interview.dto.InterviewQuestionResponseDto;
import com.server.mcp.service.InterviewQuestionAiService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewQuestionAiController {
    private final InterviewQuestionAiService interviewQuestionAiService;

    @PostMapping("/{interviewId}/questions/auto-generate")
    public List<InterviewQuestionResponseDto> generateQuestions(
            @PathVariable Long interviewId,
            @RequestParam(required = false, defaultValue = "10") Integer count
    ) {
        int questionCount = (count == null || count <= 0) ? 10 : count;
        return interviewQuestionAiService.generateQuestionsByAi(interviewId, questionCount);
    }
}
