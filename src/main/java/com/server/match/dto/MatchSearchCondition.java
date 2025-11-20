package com.server.match.dto;

import com.server.match.domain.MatchSortType;
import com.server.match.domain.MatchStatus;

public record MatchSearchCondition(
        Long jdId,
        MatchStatus status,
        MatchSortType matchSort
) {
    public MatchSortType getSortSafe() {
        return matchSort != null ? matchSort : MatchSortType.LATEST;
    }
}

