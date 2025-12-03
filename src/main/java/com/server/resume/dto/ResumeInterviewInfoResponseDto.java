package com.server.resume.dto;

import java.time.LocalDate;

public record ResumeInterviewInfoResponseDto (
        String name,
        String email,
        String phoneNumber,
        LocalDate birthDate,
        String avatar,
        String jdTitle
) {
}
