package com.server.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequestDto(

        @NotBlank
        String refreshToken
) {
}

