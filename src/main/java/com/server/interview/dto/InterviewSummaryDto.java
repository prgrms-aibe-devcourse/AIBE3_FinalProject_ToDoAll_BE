package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewSummaryDto(
        @Schema(description = "인터뷰 ID", example = "2010")
        Long interviewId,

        @Schema(description = "채용 공고 ID", example = "12")
        Long jdId,

        @Schema(description = "채용 공고 제목", example = "백엔드 개발자 (Spring)")
        String jdTitle,

        @Schema(description = "이력서 ID", example = "1")
        Long resumeId,

        @Schema(description = "후보자 이름", example = "홍길동")
        String candidateName,

        @Schema(description = "면접 상태", example = "SCHEDULED")
        String status,

        @Schema(description = "면접 결과 상태", example = "PENDING")
        String resultStatus,

        @Schema(description = "후보자 프로필 이미지", example = "https://...")
        String candidateAvatar,

        @Schema(description = "면접관 이름 리스트")
        List<String> interviewers,

        @Schema(description = "예정 일시", example = "2025-11-21T10:00:00")
        LocalDateTime scheduledAt,

        @Schema(description = "생성 일시", example = "2025-11-12T09:20:00")
        LocalDateTime createdAt
) {}
