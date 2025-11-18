package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record InterviewResultUpdateRequestDto(

        @NotNull(message = "평가 결과(result)는 필수 입력 값입니다.")
        @Schema(description = "합격, 불합격, 보류 결과", example = "PASS")
        String result

) {}