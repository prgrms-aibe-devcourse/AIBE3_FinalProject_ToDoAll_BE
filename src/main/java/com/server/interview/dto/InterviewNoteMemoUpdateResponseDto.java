package com.server.interview.dto;

import java.time.LocalDateTime;

public record InterviewNoteMemoUpdateResponseDto(
        Long memoId,
        String content,
        LocalDateTime updatedAt
) {}
