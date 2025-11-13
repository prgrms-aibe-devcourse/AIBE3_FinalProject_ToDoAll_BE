package com.server.match.dto;

import com.server.match.domain.MatchSortType;
import com.server.match.domain.MatchStatus;

public record MatchSearchCondition(
        Long jdId,
        MatchStatus status,
        MatchSortType sort,
        Integer limit,
        Integer offset
) {
    public int getPage() {
        return offset != null && offset >= 0 ? offset / getPageSize() : 0;
    }

    public int getPageSize() {
        return limit != null && offset >= 0 ? limit : 20;
    }

    public MatchSortType getSortSafe() {
        return sort != null ? sort : MatchSortType.LATEST;
    }
}

