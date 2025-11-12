package com.server.resume.dto;

import java.time.LocalDate;
import java.util.List;

public record ResumeCreateRequestDto(
        String name,
        Long jobId,
        String jobTitle,
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