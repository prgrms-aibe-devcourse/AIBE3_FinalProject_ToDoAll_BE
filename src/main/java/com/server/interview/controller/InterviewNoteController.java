package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewNoteSearchResponseDto;
import com.server.interview.service.InterviewNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews/{interviewId}/note")
@RequiredArgsConstructor
@Tag(name = "InterviewNoteController", description = "API 면접 노트 컨트롤러")
public class InterviewNoteController {

    private final InterviewNoteService interviewNoteService;

    @GetMapping
    @Operation(summary = "인터뷰 노트 조회", description = "인터뷰의 노트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인터뷰 노트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 인터뷰입니다."),
            @ApiResponse(responseCode = "403", description = "면접을 조회할 권한이 없습니다.")
    })
    public CommonResponse<InterviewNoteSearchResponseDto> getNote(
            @PathVariable Long interviewId
    ){
        InterviewNoteSearchResponseDto response = interviewNoteService.getNote(interviewId);
        return CommonResponse.success(response);
    }
}
