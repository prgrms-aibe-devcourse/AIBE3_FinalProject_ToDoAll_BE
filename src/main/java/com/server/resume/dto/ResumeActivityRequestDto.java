package com.server.resume.dto;

import com.server.resume.domain.ResumeActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public record ResumeActivityRequestDto(

        @NotBlank(message = "활동 시작일을 입력해주세요.")
        String title,

        @NotNull(message = "활동 유형을 입력해주세요.")
        ResumeActivityType type,

        @NotBlank(message = "활동 내용을 입력해주세요.")
        String organization
) { }
