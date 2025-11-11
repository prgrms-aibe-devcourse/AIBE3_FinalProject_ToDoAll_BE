package com.server.resume.dto;

import java.time.LocalDate;
import java.util.List;

public record ResumeCreateRequestDto(
        // 여기 아마 엔티티 필드 바껴서 다시 맞춰줘야 할거같아요
        String name,
        String gender,
        LocalDate birthDate,
        String email,
        String phone,
        String address,
        String detailAddress,
        List<String> education,
        List<String> experience,
        List<String> skills,
        List<String> activities,
        List<String> certifications,
        String resumeFileUrl,
        String portfolioFileUrl
) {}