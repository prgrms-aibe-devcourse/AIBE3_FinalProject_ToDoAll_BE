package com.server.jd.dto;

import java.time.LocalDate;
import java.util.List;

public record JobDescriptionResponseDto(
        // 필요한 필드는 본인이 직접 수정 및 추가
        Long id,
        String title,
        String department,
        String region,
        String workType,
        String experience,
        String education,
        String salary,
        String description,
        LocalDate deadline,
        List<String> requiredSkills,
        List<String> preferredSkills,
        String welfare,
        String status,
        Long applicantCount
) {}
