package com.server.interview.dto;

import java.util.List;

public record InterviewProfileResponseDto(
        List<String> skills,
        List<String> missingSkills,
        List<String> experiences
) {
}
