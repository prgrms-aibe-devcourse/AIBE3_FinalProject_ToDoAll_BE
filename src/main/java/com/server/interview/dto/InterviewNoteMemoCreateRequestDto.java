package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record InterviewNoteMemoCreateRequestDto(
        @NotBlank(message = "메모 내용을 입력해주세요.")
        @Schema(description = "메모 내용", example = "지원자의 문제 해결 과정이 체계적이었음.")
        String content
) {
}
