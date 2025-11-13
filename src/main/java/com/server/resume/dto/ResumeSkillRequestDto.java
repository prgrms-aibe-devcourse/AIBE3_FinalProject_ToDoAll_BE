package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResumeSkillRequestDto (
        @NotBlank(message = "기술 이름을 입력해주세요.")
        String skillName,

        @NotNull (message = "숙련도를 입력해주세요.")
        ProficiencyLevel proficiencyLevel
) {}