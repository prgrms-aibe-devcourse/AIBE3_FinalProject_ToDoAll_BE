package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "인터뷰 등록", description = "이력서에 해당하는 인터뷰를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 등록 성공"),
            @ApiResponse(responseCode = "404", description = "채용공고 또는 이력서를 찾을 수 없음")
    })
    public CommonResponse<InterviewCreateResponseDto> createInterview (
            @RequestBody InterviewCreateRequestDto interviewCreateRequestDto
    ){
        InterviewCreateResponseDto interviewCreateResponseDto = interviewService.create(interviewCreateRequestDto);
        return CommonResponse.success(interviewCreateResponseDto);
    }
}
