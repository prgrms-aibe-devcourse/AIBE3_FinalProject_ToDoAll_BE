package com.server.resume.dto;

import com.server.resume.domain.ResumeStatus;
import jakarta.validation.constraints.NotNull;

public record ResumeStatusUpdateDto(
    @NotNull(message = "변경할 상태를 입력해 주세요.")
    ResumeStatus resumeStatus
) {}

