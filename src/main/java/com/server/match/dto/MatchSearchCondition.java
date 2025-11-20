package com.server.match.dto;

import com.server.match.domain.MatchSortType;
import com.server.match.domain.MatchStatus;

public record MatchSearchCondition(
        Long jdId,
        MatchStatus status,
        MatchSortType sort
) {
    public MatchSortType getSortSafe() {
        return sort != null ? sort : MatchSortType.LATEST;
    }
}

