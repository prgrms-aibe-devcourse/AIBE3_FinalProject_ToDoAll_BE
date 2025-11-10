package com.server.jd.dto;

import com.server.jd.domain.JobStatus;

import java.time.LocalDate;
import java.util.List;

public record JobDescriptionListResponseDto(
        Long id,
        String title,
        String location,
        Long applicantCount,
        JobStatus status,
        List<String> requiredSkills,
        LocalDate startDate,
        LocalDate deadline
) {
}
