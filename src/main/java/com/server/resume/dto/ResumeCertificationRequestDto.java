package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeCertificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ResumeCertificationRequestDto(
        @NotBlank(message = "자격증/어학/수상 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "자격증/어학/수상 유형을 입력해주세요.")
        ResumeCertificationType type,

        @Positive(message = "점수/등급은 양수여야 합니다.")
        String scoreOrLevel
) {}