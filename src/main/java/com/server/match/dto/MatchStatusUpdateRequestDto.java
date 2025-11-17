package com.server.match.dto;

import com.server.match.domain.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record MatchStatusUpdateRequestDto(
        @NotNull(message = "상태는 필수 값입니다.")
        MatchStatus status
) {}
