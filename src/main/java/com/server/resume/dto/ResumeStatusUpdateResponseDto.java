package com.server.resume.dto;

import com.server.resume.domain.ResumeStatus;

public record ResumeStatusUpdateResponseDto(
        Long id,
        ResumeStatus status
) {
    public static ResumeStatusUpdateResponseDto from(Long id, ResumeStatus status) {
        return new ResumeStatusUpdateResponseDto(id, status);
    }
}
