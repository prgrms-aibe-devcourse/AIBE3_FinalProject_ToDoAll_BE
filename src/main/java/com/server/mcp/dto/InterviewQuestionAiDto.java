package com.server.mcp.dto;

public record InterviewQuestionAiDto(
        Long id,
        Long interviewId,
        String questionText,
        String type,
        String status,
        boolean checked
) {
}
