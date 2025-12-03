package com.server.interview.domain;

import jakarta.validation.constraints.NotNull;

public record InviteByResumeRequestDto (
    @NotNull
    Long resumeId
) {}
