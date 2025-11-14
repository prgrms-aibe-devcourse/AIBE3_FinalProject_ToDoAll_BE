package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.dto.InterviewSearchCondition;
import com.server.interview.dto.InterviewListResponseDto;
import com.server.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody @Valid InterviewCreateRequestDto interviewCreateRequestDto
    ){
        InterviewCreateResponseDto interviewCreateResponseDto = interviewService.create(interviewCreateRequestDto);
        return CommonResponse.success(interviewCreateResponseDto);
    }

    @GetMapping
    @Operation(summary = "인터뷰 조회", description = "인터뷰를 기본 6개씩 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태(status) 값입니다.")
    })
    public CommonResponse<InterviewListResponseDto> getInterviews (
            @ModelAttribute @Valid InterviewSearchCondition condition
    ){
        InterviewListResponseDto interviewSearchResponseDto = interviewService.getInterviews(condition);
        return CommonResponse.success(interviewSearchResponseDto);
    }
}
