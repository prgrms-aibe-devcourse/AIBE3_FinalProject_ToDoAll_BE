package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeSkill;

public record ResumeSkillResponseDto (
        String skillName,
        ProficiencyLevel proficiencyLevel
) {

        public static ResumeSkillResponseDto fromEntity(ResumeSkill skill) {
            return new ResumeSkillResponseDto(skill.getSkill().getName(), skill.getProficiencyLevel());
        }
}
