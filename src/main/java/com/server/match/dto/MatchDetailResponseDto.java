package com.server.match.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MatchDetailResponseDto(
        String jdTitle,
        String resumeName,
        Float matchScore,
        String skillMatchRate,
        List<String> missingSkills,
        String recommendationReason,
        String resumeSummary
) {
    public MatchDetailResponseDto {
        // null 체크 후 백분율로 변환
        if (matchScore != null) {
            matchScore = Math.round(matchScore * 1000f) / 10f;
        } else {
            matchScore = 0f;
        }
    }
}