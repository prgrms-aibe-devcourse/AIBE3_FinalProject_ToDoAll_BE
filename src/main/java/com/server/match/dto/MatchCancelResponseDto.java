package com.server.match.dto;

public record MatchCancelResponseDto(
        Long matchId,
        String jdTitle,
        String resumeName
) {}
