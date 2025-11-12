package com.server.match.dto;

import jakarta.validation.constraints.NotNull;

public record MatchRequestDto(
        @NotNull Long jdId,        // 채용공고 ID
        @NotNull Long resumeId     // 이력서 ID
) {}
