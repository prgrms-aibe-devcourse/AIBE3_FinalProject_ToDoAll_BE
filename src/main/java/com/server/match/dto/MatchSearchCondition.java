package com.server.match.dto;

import com.server.match.domain.MatchStatus;


public record MatchSearchCondition(
        Long jdId,
        MatchStatus status
) {}

