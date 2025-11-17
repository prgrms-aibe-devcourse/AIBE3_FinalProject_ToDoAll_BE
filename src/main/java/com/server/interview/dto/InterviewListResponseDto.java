package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record InterviewListResponseDto(

        @Schema(description = "인터뷰 요약 데이터 목록", implementation = InterviewSummaryDto.class)
        List<InterviewSummaryDto> data,

        @Schema(description = "다음 페이지 조회를 위한 커서 ID. null이면 더 이상 조회할 데이터 없음.", example = "2001")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {}

