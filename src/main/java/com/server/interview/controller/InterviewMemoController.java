package com.server.interview.controller;

import com.server.interview.service.InterviewNoteMemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/memos")
@RequiredArgsConstructor
public class InterviewMemoController {

    private final InterviewNoteMemoService interviewNoteMemoService;

}
