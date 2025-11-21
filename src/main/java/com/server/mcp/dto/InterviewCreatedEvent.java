package com.server.mcp.dto;

public record InterviewCreatedEvent(
        Long interviewId,
        Long resumeId,
        Long jdId
) {
}
