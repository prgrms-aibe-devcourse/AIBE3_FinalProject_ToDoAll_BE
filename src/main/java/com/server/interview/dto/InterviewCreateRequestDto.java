package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewCreateRequestDto (
        @Schema(description = "JD ID", example = "1")
        Long jd_id,

        @Schema(description = "이력서 ID", example = "1")
        Long resume_id,

        @Schema(description = "참여자 ID 목록", example = "[2, 3, 4]")
        List<Long> participant_ids,

        @Schema(description = "예정 일시 (ISO 8601)", example = "2025-12-01T10:00:00")
        LocalDateTime scheduledAt
){
}
