package com.server.interview.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/questions")
@RequiredArgsConstructor
@Tag(name = "InterviewQuestionController", description = "API 면접 질문 컨트롤러")
public class InterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;
}
