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
        String benefits,
        String location,
        String thumbnailUrl,
        Long authorId,
        List<String> requiredSkills,
        List<String> preferredSkills
) {}