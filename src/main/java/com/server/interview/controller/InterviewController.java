package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.*;
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
            @ModelAttribute @Valid InterviewSearchConditionDto condition
    ){
        InterviewListResponseDto interviewSearchResponseDto = interviewService.getInterviews(condition);
        return CommonResponse.success(interviewSearchResponseDto);
    }

    @DeleteMapping("/{interviewId}")
    @Operation(summary = "면접 삭제", description = "면접 ID를 통해 면접을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접입니다."),
            @ApiResponse(responseCode = "403", description = "면접을 삭제할 권한이 없습니다.")
    })
    public CommonResponse<String> deleteInterview (
            @PathVariable("interviewId") Long interviewId
    ){
        interviewService.deleteInterview(interviewId);
        return CommonResponse.success("면접 삭제 완료");
    }

    @PatchMapping("/{interviewId}/end")
    @Operation(summary = "면접 종료", description = "면접을 DONE 상태로 변경하고 AI 요약 생성을 비동기로 시작합니다.")
    public CommonResponse<Void> finishInterview(@PathVariable Long interviewId) {
        interviewService.finishInterview(interviewId);
        return CommonResponse.success(null);
    }


    @GetMapping("/{interviewId}/interview-profile")
    @Operation(summary = "인터뷰 프로필 조회", description = "면접에 연결된 이력서/공고 정보를 기반으로 프로필 정보를 반환합니다.")
    public CommonResponse<InterviewProfileResponseDto> getInterviewProfile(
            @PathVariable Long interviewId
    ) {
        InterviewProfileResponseDto dto = interviewService.getInterviewProfile(interviewId);
        return CommonResponse.success(dto);
    }
}
