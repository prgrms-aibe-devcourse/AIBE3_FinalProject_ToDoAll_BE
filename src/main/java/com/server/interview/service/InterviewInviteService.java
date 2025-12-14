package com.server.interview.service;

import com.server.interview.dto.InviteResponseDto;
import com.server.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class InterviewInviteService {

    private final ResumeService resumeService;
    private final InterviewInviteTokenService interviewInviteTokenService;
    private final InterviewInviteMailService mailService;

    @Value("${app.email.auth-expiry-minutes:30}")
    private int expiryMinutes;

    @Value("${app.interview.frontend-base-url:https://jobda.vercel.app}")
    private String guestChatBaseUrl;

    public InviteResponseDto inviteByResume(Long interviewId, Long resumeId) {
        ResumeService.ResumeInviteInfo info = resumeService.getInviteInfo(resumeId);

        String token = interviewInviteTokenService.createToken(interviewId, info.resumeId(), info.email());

        String link = UriComponentsBuilder
                .fromHttpUrl(guestChatBaseUrl)
                .path("/interview/{interviewId}/chat-room/guest")
                .queryParam("token", token)
                .buildAndExpand(interviewId)
                .toUriString();

        String jdTitle = (info.jdTitle() == null || info.jdTitle().isBlank())
                ? "Jobda"
                : info.jdTitle();

        try {
            mailService.sendInviteEmail(info.email(), jdTitle, info.name(), link, expiryMinutes);
        } catch (RuntimeException e) {
            try {
                interviewInviteTokenService.deleteTokenOrThrow(token);
            } catch (RuntimeException ignore) {
            }
            throw e;
        }

        return new InviteResponseDto(interviewId, info.email(), token);
    }
}
