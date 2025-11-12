package com.server.match.dto;

import com.server.match.domain.MatchStatus;

public record MatchResponseDto(
        Long matchId,
        MatchStatus status
) {}
