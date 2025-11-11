package com.server.jd.dto;

import java.time.LocalDate;
import java.util.List;

public record JobDescriptionCreateRequestDto(
        String title,
        String department,
        String workType,
        String experience,
        String education,
        String salary,
        String description,
        LocalDate deadline,
        String welfare,
        String location,
        String thumbnailUrl,
        Long authorId,
        List<Long> requiredSkillIds,
        List<Long> preferredSkillIds
) {}