package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;

public record ResumeSkillRequestDto (
    String skillName,
    ProficiencyLevel proficiencyLevel
) {}