package com.server.admin.dto;

import com.server.interview.domain.Interview;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminInterviewDetailDto(
        Long id,
        String jdTitle,
        String candidateName,
        LocalDateTime scheduledAt,
        String status,
        String result,
        boolean deleted,
        List<String> ownedSkills,
        List<String> missingSkills,
        List<String> experiences,
        String summary
) {
    public static AdminInterviewDetailDto of(
            Interview interview,
            List<String> owned,
            List<String> missing,
            List<String> experiences
    ) {
        return AdminInterviewDetailDto.builder()
                .id(interview.getId())
                .jdTitle(interview.getJobDescription().getTitle())
                .candidateName(interview.getResume().getName())
                .scheduledAt(interview.getScheduledAt())
                .status(interview.getStatus().name())
                .result(interview.getResult().name())
                .deleted(interview.isDeleted())
                .ownedSkills(owned)
                .missingSkills(missing)
                .experiences(experiences)
                .summary(interview.getSummary())
                .build();
    }
}