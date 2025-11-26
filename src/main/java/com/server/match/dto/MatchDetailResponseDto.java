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
) {}