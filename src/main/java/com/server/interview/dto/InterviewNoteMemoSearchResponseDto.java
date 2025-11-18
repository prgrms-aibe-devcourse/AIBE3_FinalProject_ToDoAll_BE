package com.server.interview.dto;

import com.server.interview.domain.InterviewNoteMemo;

import java.time.LocalDateTime;

public record InterviewNoteMemoSearchResponseDto(
        Long memoId,
        String content,
        Author author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static InterviewNoteMemoSearchResponseDto from(InterviewNoteMemo memo) {
        return new InterviewNoteMemoSearchResponseDto(
                memo.getId(),
                memo.getContent(),
                new Author(
                        memo.getAuthor().getId(),
                        memo.getAuthor().getName()
                ),
                memo.getCreatedAt(),
                memo.getUpdatedAt()
        );
    }
    public record Author(
            Long userId,
            String name
    ) {}
}
