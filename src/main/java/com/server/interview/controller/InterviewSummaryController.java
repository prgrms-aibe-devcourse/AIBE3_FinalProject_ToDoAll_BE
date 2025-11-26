package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.mcp.service.InterviewSummaryAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview Summary", description = "면접 요약 관련 API")
public class InterviewSummaryController {

    private final InterviewSummaryAiService interviewSummaryAiService;
    private final InterviewParticipantRepository interviewParticipantRepository;

    @PostMapping("/{interviewId}/summary/ai")
    @Operation(summary = "AI 면접 요약 생성 요청", description = "해당 면접에 대해 AI 요약 생성을 요청합니다.")
    public CommonResponse<Void> generateAiSummary(
            @PathVariable Long interviewId
            // @AuthenticationPrincipal CustomUserDetails user   // 권한 체크 필요 시 추가
    ) {
        // TODO: organizer 또는 인터뷰 참여자 여부 확인 로직 추가 예정
        interviewSummaryAiService.requestAutoSummary(interviewId);
        return CommonResponse.success(null);
    }
}

