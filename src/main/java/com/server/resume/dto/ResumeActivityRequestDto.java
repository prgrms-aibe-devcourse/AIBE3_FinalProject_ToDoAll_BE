package com.server.resume.dto;

import com.server.resume.domain.ResumeActivityType;

import java.time.LocalDate;

public record ResumeActivityRequestDto(
        String title,
        ResumeActivityType type,
        String organization
) { }
