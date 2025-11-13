package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "InterviewController", description = "API 면접 컨트롤러")
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping
    public CommonResponse<InterviewCreateResponseDto> createInterview (
            @RequestBody InterviewCreateRequestDto interviewCreateRequestDto
    ){
        InterviewCreateResponseDto interviewCreateResponseDto = interviewService.create(interviewCreateRequestDto);
        return CommonResponse.success(interviewCreateResponseDto);
    }
}
