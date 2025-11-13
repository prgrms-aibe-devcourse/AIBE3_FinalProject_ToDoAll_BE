package com.server.resume.dto;

import com.server.resume.domain.ProficiencyLevel;
import com.server.resume.domain.ResumeCertificationType;

public record ResumeCertificationRequestDto(
        String name,
        ResumeCertificationType type,
        String scoreOrLevel
) {}