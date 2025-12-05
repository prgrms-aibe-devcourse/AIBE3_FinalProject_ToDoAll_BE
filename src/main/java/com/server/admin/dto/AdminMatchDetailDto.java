package com.server.admin.dto;

import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.MatchDetailResponseDto;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminMatchDetailDto(
        Long id,
        Long jdId,
        String jdTitle,
        String jdStatus,
        Long resumeId,
        String resumeName,
        String resumeEmail,
        String resumePhone,
        MatchStatus status,
        LocalDateTime appliedAt,
        Float matchScore,
        boolean deleted,
        String resumeSummary,
        String skillMatchRate,
        List<String> missingSkills,
        List<String> skills
) {

    public static AdminMatchDetailDto of(
            Match match,
            MatchDetailResponseDto detail
    ) {
        return AdminMatchDetailDto.builder()
                .id(match.getId())
                .jdId(match.getJobDescription().getId())
                .jdTitle(match.getJobDescription().getTitle())
                .jdStatus(match.getJobDescription().getStatus() != null
                        ? match.getJobDescription().getStatus().name()
                        : null)
                .resumeId(match.getResume().getId())
                .resumeName(match.getResume().getName())
                .resumeEmail(match.getResume().getEmail())
                .resumePhone(match.getResume().getPhone())
                .status(match.getStatus())
                .appliedAt(match.getAppliedAt())
                .matchScore(match.getMatchScore())
                .deleted(match.isDeleted())
                .resumeSummary(detail.resumeSummary())
                .skillMatchRate(detail.skillMatchRate())
                .missingSkills(detail.missingSkills())
                .skills(detail.skills())
                .build();
    }
}