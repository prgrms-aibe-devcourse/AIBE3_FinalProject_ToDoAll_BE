package com.server.match.dto;

import com.server.match.domain.MatchStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record MatchListResponseDto(
        Long resumeId,
        String resumeName,
        Float matchScore,
        MatchStatus status,
        String skillMatchRate,
        List<String> missingSkills,
        String resumeSummary
) {}
