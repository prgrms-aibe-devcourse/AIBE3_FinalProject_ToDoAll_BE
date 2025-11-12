package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeActivity;

public record ResumeActivityRequestDto(
        String title,
        ProficiencyLevel proficiencyLevel,
        String organization
) { }
