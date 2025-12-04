package com.server.interview.dto;

import jakarta.validation.constraints.NotNull;

public record InviteByResumeRequestDto (
    @NotNull
    Long resumeId
) {}
