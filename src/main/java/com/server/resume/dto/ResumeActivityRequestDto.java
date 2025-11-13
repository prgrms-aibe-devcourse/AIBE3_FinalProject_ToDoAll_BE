package com.server.resume.dto;

import com.server.resume.domain.ResumeActivityType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ResumeActivityRequestDto(

        @NotBlank(message = "활동 시작일을 입력해주세요.")
        String title,

        @NotBlank(message = "활동 종료일을 입력해주세요.")
        ResumeActivityType type,

        @NotBlank(message = "활동 내용을 입력해주세요.")
        String organization
) { }
