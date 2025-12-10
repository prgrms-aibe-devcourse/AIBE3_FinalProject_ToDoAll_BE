package com.server.match.dto;

import com.server.match.domain.MatchStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record MatchListResponseDto(
        Long resumeId,
        String resumeName,
        String resumeProfileImage,
        Float matchScore,
        MatchStatus status,
        String skillMatchRate,
        List<String> missingSkills,
        List<String> skills,
        String resumeSummary
) {
    public MatchListResponseDto {
        // null 체크 후 백분율로 변환
        if (matchScore != null) {
            matchScore = Math.round(matchScore * 1000f) / 10f;
        } else {
            matchScore = 0f;
        }
    }
}
