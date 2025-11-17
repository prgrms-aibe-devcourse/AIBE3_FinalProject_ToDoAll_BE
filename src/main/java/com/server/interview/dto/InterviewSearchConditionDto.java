package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record InterviewSearchConditionDto(
        @Schema(description = "조회할 채용공고 ID, 미입력 시 전체 조회", example = "12")
        Long jdId,

        @Schema(description = "면접 상태 (ALL/WAITING/IN_PROGRESS/DONE)", example = "WAITING")
        @Pattern(regexp = "ALL|WAITING|IN_PROGRESS|DONE", message = "유효하지 않은 상태(status) 값입니다.")
        String status,

        @Schema(description = "한 번에 조회할 개수(기본값:6)", example = "6")
        @Min(value = 1, message = "limit 값은 1 이상이어야 합니다.")
        Integer limit,

        @Schema(description = "다음 페이지 조회용 커서 ID", example = "2001")
        Long cursor,

        @Schema(description = "정렬 기준 (`createdAt,desc` / `createdAt,asc`)", example = "createdAt,desc")
        @Pattern(regexp = "createdAt,(desc|asc)", message = "정렬 값(sort)이 올바르지 않습니다.")
        String sort
) {}