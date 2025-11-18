package com.server.interview.dto;

import java.time.LocalDateTime;

public record InterviewNoteSearchResponseDto (
        Long noteId,
        Long interviewId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
