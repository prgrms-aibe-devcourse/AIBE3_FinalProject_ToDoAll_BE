package com.server.jd.dto;

import com.server.jd.domain.JobStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record JobDescriptionDetailResponseDto(
        Long id,
        String title,
        String department,
        String workType,
        String experience,
        String education,
        String salary,
        String description,
        LocalDate startDate,
        LocalDate deadline,
        JobStatus status,
        String benefits,
        Long applicantCount,
        String location,
        String thumbnailUrl,
        List<String> skills,
        List<String> preferredSkills
) {
}
