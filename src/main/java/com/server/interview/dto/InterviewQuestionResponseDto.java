package com.server.interview.dto;

import java.time.LocalDateTime;

public record InterviewQuestionResponseDto (
        Long questionId,
        String questionType,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean checked
){
}
