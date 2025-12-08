package com.server.jd.dto;

import com.server.jd.domain.JobStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record JobDescriptionListResponseDto(
        Long id,
        String title,
        String location,
        Long applicantCount,
        JobStatus status,
        List<String> requiredSkills,
        LocalDate startDate,
        LocalDate deadline,
        String thumbnailUrl
) {
}
