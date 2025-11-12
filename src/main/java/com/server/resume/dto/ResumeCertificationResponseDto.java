package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeCertification;
import com.server.resume.domain.ResumeCertificationType;

public record ResumeCertificationResponseDto(
        String name,
        ResumeCertificationType type,
        String scoreOrLevel
) {
    public static ResumeCertificationResponseDto fromEntity(ResumeCertification certification) {
        return new ResumeCertificationResponseDto(certification.getName(), certification.getType(), certification.getScoreOrLevel());
    }
}