package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record InterviewNoteMemoUpdateRequestDto(
        @NotBlank(message = "메모 내용을 입력해주세요.")
        @Schema(description = "메모 내용", example = "이전에 작성한 메모를 보완해서, 의사소통 능력이 매우 뛰어남.")
        String content
) {}