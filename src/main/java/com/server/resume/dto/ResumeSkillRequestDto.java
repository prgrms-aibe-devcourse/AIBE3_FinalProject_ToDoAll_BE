package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import jakarta.validation.constraints.NotBlank;

public record ResumeSkillRequestDto (
        @NotBlank(message = "기술 이름을 입력해주세요.")
        String skillName,

        @NotBlank(message = "숙련도를 입력해주세요.")
        ProficiencyLevel proficiencyLevel
) {}