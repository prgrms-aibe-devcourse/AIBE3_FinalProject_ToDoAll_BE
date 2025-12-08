package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.mcp.service.InterviewSummaryAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.server.interview.service.InterviewSummaryQueryService;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview Summary", description = "면접 요약 관련 API")
public class InterviewSummaryController {

    private final InterviewSummaryAiService interviewSummaryAiService;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final InterviewSummaryQueryService interviewSummaryQueryService; // ★ MUST HAVE


    @PostMapping("/{interviewId}/summary/ai")
    @Operation(summary = "AI 면접 요약 생성 요청", description = "해당 면접에 대해 AI 요약 생성을 요청합니다.")
    public CommonResponse<Void> generateAiSummary(
            @PathVariable Long interviewId
            // @AuthenticationPrincipal CustomUserDetails user   // 권한 체크 필요 시 추가
    ) {
        interviewSummaryAiService.generateSummary(interviewId);
        return CommonResponse.success(null);
    }

    @GetMapping("/{interviewId}/summary")
    @Operation(summary = "면접 요약 조회", description = "저장된 AI 면접 요약을 조회합니다.")
    public CommonResponse<String> getSummary(@PathVariable Long interviewId) {
        String summary = interviewSummaryQueryService
                .getSummary(interviewId)
                .orElse("");
        return CommonResponse.success(summary);
    }

}

