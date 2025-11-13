package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ResumeExperienceRequestDto(

        @NotBlank(message = "회사 이름을 입력해주세요.")
        String companyName,

        @NotBlank(message = "부서 이름을 입력해주세요.")
        String department,

        @NotBlank(message = "직무를 입력해주세요.")
        String position,

        @NotBlank(message = "근무 시작일을 입력해주세요.")
        LocalDate startDate,

        @NotBlank(message = "근무 종료일을 입력해주세요.")
        LocalDate endDate
) { }
