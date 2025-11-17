package com.server.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record InterviewNoteMemoUpdateRequestDto(
        @NotBlank(message = "메모 내용을 입력해주세요.")
        String content
) {}