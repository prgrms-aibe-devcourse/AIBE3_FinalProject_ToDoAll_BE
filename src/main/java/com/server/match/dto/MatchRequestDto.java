package com.server.match.dto;

import jakarta.validation.constraints.NotNull;

public record MatchRequestDto(
        @NotNull(message = "채용공고 ID는 필수입니다.")
        Long jdId,        // 채용공고 ID
        @NotNull(message = "이력서 ID는 필수입니다.")
        Long resumeId     // 이력서 ID
) {}
