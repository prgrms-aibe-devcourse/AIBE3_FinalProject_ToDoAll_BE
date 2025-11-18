package com.server.interview.dto;

public record InterviewEvaluationCreateResponseDto(
        Long evaluationId,
        Integer scoreTech,
        Integer scoreComm,
        Double scoreOverall,
        String comment
) {}