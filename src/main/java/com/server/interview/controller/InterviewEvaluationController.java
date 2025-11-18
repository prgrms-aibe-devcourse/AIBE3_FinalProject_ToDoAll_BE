package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.*;
import com.server.interview.service.InterviewEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "인터뷰 평가 등록", description = "인터뷰에 해당하는 평가를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "평가 등록 성공"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public CommonResponse<InterviewEvaluationCreateResponseDto> createEvaluation(
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewEvaluationCreateRequestDto request
    ) {
        InterviewEvaluationCreateResponseDto response = interviewEvaluationService.create(interviewId, request);

        return CommonResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "인터뷰 평가 조회", description = "인터뷰에 해당하는 평가를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "평가 조회 성공"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public CommonResponse<InterviewEvaluationSearchResponseDto> getEvaluation(
            @PathVariable Long interviewId
    ) {
        InterviewEvaluationSearchResponseDto response = interviewEvaluationService.getEvaluations(interviewId);
        return CommonResponse.success(response);
    }

    @PatchMapping("/{evaluationId}")
    @Operation(summary = "인터뷰 평가 수정", description = "인터뷰에 해당하는 평가를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "평가 조회 성공"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public CommonResponse<InterviewEvaluationUpdateResponseDto> update(
            @PathVariable Long interviewId,
            @PathVariable Long evaluationId,
            @Valid @RequestBody InterviewEvaluationUpdateRequestDto request
    ) {

        InterviewEvaluationUpdateResponseDto response = interviewEvaluationService.update(interviewId, evaluationId, request);
        return CommonResponse.success(response);
    }

    @PatchMapping("/{evaluationId}/result")
    @Operation(summary = "인터뷰 결과 등록", description = "인터뷰의 결과를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 등록 성공"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public CommonResponse<InterviewResultUpdateResponseDto> updateResult(
            @PathVariable Long interviewId,
            @PathVariable Long evaluationId,
            @Valid @RequestBody InterviewResultUpdateRequestDto request
    ) {
        InterviewResultUpdateResponseDto response =
                interviewEvaluationService.updateResult(interviewId, evaluationId, request);

        return CommonResponse.success(response);
    }
}
