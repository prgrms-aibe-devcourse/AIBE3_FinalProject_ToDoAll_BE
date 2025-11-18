package com.server.interview.dto;

public record InterviewEvaluationUpdateResponseDto(
        Long evaluationId,
        Integer scoreTech,
        Integer scoreComm,
        Double scoreOverall,
        String comment
) {}