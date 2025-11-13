package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeExperience;

import java.time.LocalDate;

public record ResumeExperienceResponseDto(
        String companyName,
        String department,
        String position,
        LocalDate startDate,
        LocalDate endDate
) {
    public static ResumeExperienceResponseDto fromEntity(ResumeExperience experience) {
        return new ResumeExperienceResponseDto(
                experience.getCompanyName(),
                experience.getDepartment(),
                experience.getPosition(),
                experience.getStartDate(),
                experience.getEndDate()
        );
    }
}
