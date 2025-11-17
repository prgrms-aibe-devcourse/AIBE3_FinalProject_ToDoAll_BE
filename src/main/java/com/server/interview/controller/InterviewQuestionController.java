package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewQuestionResponseDto;
import com.server.interview.dto.InterviewQuestionUpdateRequestDto;
import com.server.interview.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/questions")
@RequiredArgsConstructor
@Tag(name = "InterviewQuestionController", description = "API 면접 질문 컨트롤러")
public class InterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    @PutMapping
    @Operation(summary = "인터뷰 질문 업데이트", description = "인터뷰 질문을 삭제, 수정, 등록 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접입니다."),
            @ApiResponse(responseCode = "403", description = "질문을 등록할 권한이 없습니다."),
            @ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않습니다.")
    })
    public CommonResponse<String> updateQuestions(
            @PathVariable Long interviewId,
            @RequestBody @Valid InterviewQuestionUpdateRequestDto request
    ) {
        interviewQuestionService.updateQuestions(interviewId, request);
        return CommonResponse.success("면접 질문이 성공적으로 업데이트되었습니다.");
    }

    @GetMapping
    @Operation(summary = "인터뷰 질문 조회", description = "인터뷰에 해당하는 질문을 전체조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 인터뷰입니다."),
            @ApiResponse(responseCode = "403", description = "질문을 조회할 권한이 없습니다.")
    })
    public CommonResponse<List<InterviewQuestionResponseDto>> getQuestions(
            @PathVariable Long interviewId
    ) {
        List<InterviewQuestionResponseDto> interviewQuestionResponseDto =  interviewQuestionService.getQuestions(interviewId);
        return CommonResponse.success(interviewQuestionResponseDto);
    }


    @PatchMapping("/{questionId}/toggle-check")
    @Operation(summary = "인터뷰 질문 체크", description = "인터뷰에 해당하는 질문의 체크 상태를 변경합니다.")
    public CommonResponse<String> toggleQuestionCheck(
            @PathVariable Long interviewId,
            @PathVariable Long questionId
    ) {

        interviewQuestionService.toggleCheck(interviewId, questionId);

        return CommonResponse.success("체크 상태 변경이 성공적으로 업데이트되었습니다.");
    }
}
