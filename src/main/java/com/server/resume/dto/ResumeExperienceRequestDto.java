package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;

import java.time.LocalDate;

public record ResumeExperienceRequestDto(
        String companyName,
        String department,
        String position,
        LocalDate startDate,
        LocalDate endDate
) { }
