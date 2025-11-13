package com.server.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record InterviewCreateResponseDto (
        @Schema(description = "면접 ID", example = "1")
        Long interviewId
){
}
