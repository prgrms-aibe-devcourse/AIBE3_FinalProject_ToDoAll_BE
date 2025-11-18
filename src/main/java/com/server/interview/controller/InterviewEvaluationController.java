package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewEvaluationCreateRequestDto;
import com.server.interview.dto.InterviewEvaluationCreateResponseDto;
import com.server.interview.service.InterviewEvaluationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/evaluation")
@RequiredArgsConstructor
@Tag(name = "InterviewEvaluationController", description = "API 면접 평가 컨트롤러")
public class InterviewEvaluationController {

    private final InterviewEvaluationService interviewEvaluationService;

    @PostMapping
    public CommonResponse<InterviewEvaluationCreateResponseDto> createEvaluation(
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewEvaluationCreateRequestDto request
    ) {
        InterviewEvaluationCreateResponseDto response = interviewEvaluationService.create(interviewId, request);

        return CommonResponse.success(response);
    }
}
