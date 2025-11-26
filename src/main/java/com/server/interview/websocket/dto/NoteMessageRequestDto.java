package com.server.interview.websocket.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteMessageRequestDto (
        @NotBlank(message = "content는 비어있을 수 없습니다.")
        String content
) {}
