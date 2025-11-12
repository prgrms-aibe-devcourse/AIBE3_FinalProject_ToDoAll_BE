package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeActivity;
import com.server.resume.domain.ResumeActivityType;
import com.server.resume.domain.ResumeSkill;

public record ResumeActivityResponseDto(
        String title,
        ResumeActivityType type,
        String organization
) {

        public static ResumeActivityResponseDto fromEntity(ResumeActivity activity) {
            return new ResumeActivityResponseDto(activity.getTitle(), activity.getType(), activity.getOrganization());
        }
}
