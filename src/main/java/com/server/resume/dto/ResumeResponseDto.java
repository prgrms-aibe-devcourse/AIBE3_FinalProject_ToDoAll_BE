package com.server.resume.dto;

public record ResumeResponseDto(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String education,
        String experience,
        String skills,
        String certifications,
        String resumeFileUrl,
        String portfolioFileUrl
) {}
