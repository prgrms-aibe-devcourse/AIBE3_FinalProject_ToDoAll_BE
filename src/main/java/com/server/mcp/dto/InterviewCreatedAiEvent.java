package com.server.mcp.dto;

public record InterviewCreatedAiEvent(
        Long interviewId,
        Long resumeId,
        Long jdId
) {
}
