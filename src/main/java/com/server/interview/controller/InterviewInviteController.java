package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InviteByResumeRequestDto;
import com.server.interview.dto.InviteResponseDto;
import com.server.interview.service.InterviewInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interviews")
public class InterviewInviteController {
    private final InterviewInviteService inviteService;

    @PostMapping("/{interviewId}/invites")
    public CommonResponse<InviteResponseDto> inviteByResume(
            @PathVariable Long interviewId,
            @RequestBody @Valid InviteByResumeRequestDto requestDto
            ) {
        return CommonResponse.success(inviteService.inviteByResume(interviewId, requestDto.resumeId()));
    }
}
