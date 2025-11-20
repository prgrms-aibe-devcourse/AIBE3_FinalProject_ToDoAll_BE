package com.server.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NotificationRequestDto(
        @NotNull
        Long userId,

        @NotBlank
        String title,

        @NotBlank
        String message,

        LocalDateTime createdAt
) {}