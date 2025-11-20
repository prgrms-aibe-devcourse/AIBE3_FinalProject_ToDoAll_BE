package com.server.match.repository;

import com.server.match.dto.MatchListResponseDto;
import com.server.match.dto.MatchSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchQueryRepository {
    Page<MatchListResponseDto> searchMatches(
            MatchSearchCondition condition,
            Pageable pageable
    );
}