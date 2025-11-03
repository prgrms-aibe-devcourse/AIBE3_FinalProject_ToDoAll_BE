package com.server.match.dto;


import lombok.Builder;

import java.util.List;

// JD-이력서 매칭 결과 DTO
@Builder
public record MatchResultDto(
        Long jdId,
        List<Long> matchedResumeIds
) {}
