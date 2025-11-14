package com.server.jd.dto;

import com.server.jd.domain.JobStatus;

public record JobDescriptionStatusRequestDto (
        JobStatus status
) {
}
