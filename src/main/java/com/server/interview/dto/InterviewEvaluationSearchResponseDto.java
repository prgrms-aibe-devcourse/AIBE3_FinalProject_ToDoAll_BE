package com.server.interview.dto;

import java.time.LocalDateTime;

public record  InterviewEvaluationSearchResponseDto (
        Long evaluationId,
        Integer scoreTech,
        Integer scoreComm,
        Double scoreOverall,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
