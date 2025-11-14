package com.server.jd.dto;

import com.server.jd.domain.JobStatus;

public record JobDescriptionStatusResponseDto(
        Long id,
        JobStatus status
) {
}
