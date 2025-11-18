package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record InterviewEvaluationCreateRequestDto(

        @NotNull(message = "기술 점수는 필수 입력 값입니다.")
        @Min(value = 0) @Max(value = 100)
        @Schema(description = "기술 점수", example = "85")
        Integer scoreTech,

        @NotNull(message = "커뮤니케이션 점수는 필수 입력 값입니다.")
        @Min(value = 0) @Max(value = 100)
        @Schema(description = "커뮤니케이션 점수", example = "90")
        Integer scoreComm,

        @NotNull(message = "종합 점수는 필수 입력 값입니다.")
        @DecimalMin(value = "0.0")   // 최소 0.0
        @DecimalMax(value = "100.0") // 최대 100.0
        @Digits(integer = 3, fraction = 1)
        @Schema(description = "종합점수", example = "88.5")
        Double scoreOverall,

        String comment
) {}