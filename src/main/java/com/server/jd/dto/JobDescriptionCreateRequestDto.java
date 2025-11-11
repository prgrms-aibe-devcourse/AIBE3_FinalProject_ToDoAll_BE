package com.server.jd.dto;

import java.time.LocalDate;
import java.util.List;

public record JobDescriptionCreateRequestDto(
        // 필드는 필요하다면 본인이 직접 수정 및 추가
        String title,
        String department,
        String workType,
        String experience,
        String education,
        String salary,
        String description,
        LocalDate deadline,
        List<String> requiredSkills,
        List<String> preferredSkills,
        String welfare,
        Long authorId
) {}
