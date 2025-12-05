package com.server.admin.dto;

import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminMatchListDto(
        Long id,
        Long jdId,
        String jdTitle,
        Long resumeId,
        String resumeName,
        MatchStatus status,
        LocalDateTime appliedAt,
        Float matchScore,
        boolean deleted
) {

    public static AdminMatchListDto from(Match match) {
        return AdminMatchListDto.builder()
                .id(match.getId())
                .jdId(match.getJobDescription().getId())
                .jdTitle(match.getJobDescription().getTitle())
                .resumeId(match.getResume().getId())
                .resumeName(match.getResume().getName())
                .status(match.getStatus())
                .appliedAt(match.getAppliedAt())
                .matchScore(match.getMatchScore())
                .deleted(match.isDeleted())
                .build();
    }
}
