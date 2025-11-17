package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.*;
import com.server.interview.service.InterviewNoteMemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/memos")
@RequiredArgsConstructor
@Tag(name = "InterviewNoteMemoController", description = "API 면접 노트 메모 컨트롤러")
public class InterviewNoteMemoController {

    private final InterviewNoteMemoService interviewNoteMemoService;

    @GetMapping
    @Operation(summary = "인터뷰 메모 조회", description = "인터뷰의 메모를 전체 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 인터뷰입니다."),
            @ApiResponse(responseCode = "403", description = "메모를 조회할 권한이 없습니다.")
    })
    public CommonResponse<List<InterviewNoteMemoSearchResponseDto>> getMemos(
            @PathVariable Long interviewId
    ) {
        List<InterviewNoteMemoSearchResponseDto> memos =
                interviewNoteMemoService.getMemos(interviewId);

        return CommonResponse.success(memos);
    }

    @PostMapping
    @Operation(summary = "인터뷰 메모 생성", description = "인터뷰의 메모를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 메모 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 인터뷰입니다."),
            @ApiResponse(responseCode = "403", description = "메모를 생성할 권한이 없습니다.")
    })
    public CommonResponse<InterviewNoteMemoCreateResponseDto> createMemo(
            @PathVariable Long interviewId,
            @RequestBody @Valid InterviewNoteMemoCreateRequestDto request
    ) {
        InterviewNoteMemoCreateResponseDto response = interviewNoteMemoService.create(interviewId, request);
        return  CommonResponse.success(response);
    }

    @PatchMapping("/{memoId}")
    @Operation(summary = "인터뷰 메모 수정", description = "인터뷰의 메모를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 메모 수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 인터뷰입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 메모입니다.")
    })
    public CommonResponse<InterviewNoteMemoUpdateResponseDto> updateMemo(
            @PathVariable Long interviewId,
            @PathVariable Long memoId,
            @Valid @RequestBody InterviewNoteMemoUpdateRequestDto request
    ) {
        var result = interviewNoteMemoService.update(interviewId, memoId, request);
        return CommonResponse.success(result);
    }
}
