package com.server.resume.dto;

public record ResumeCreateRequestDto(
        // dto 관련 필드도 필요하다면 본인이 수정 및 추가
        String name,
        String email,
        String phone,
        String address,
        String education,
        String experience,
        String skills,
        String certifications,
        String resumeFileUrl,
        String portfolioFileUrl,
        Long userId
) {}