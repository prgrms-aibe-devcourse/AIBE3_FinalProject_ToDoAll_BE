package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewNoteMemoResponseDto;
import com.server.interview.service.InterviewNoteMemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/memos")
@RequiredArgsConstructor
public class InterviewNoteMemoController {

    private final InterviewNoteMemoService interviewNoteMemoService;

    @GetMapping
    @Operation(summary = "인터뷰 메모 조회", description = "인터뷰의 메모를 전체 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 인터뷰입니다.")
    })
    public CommonResponse<List<InterviewNoteMemoResponseDto>> getMemos(
            @PathVariable Long interviewId
    ) {
        List<InterviewNoteMemoResponseDto> memos =
                interviewNoteMemoService.getMemos(interviewId);

        return CommonResponse.success(memos);
    }
}
