package com.server.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto (
        @NotBlank
        @Email
        @Size(min = 4, max = 30)
        String email,
        @NotBlank
        @Size(min = 4, max = 20)
        String password
) {
}
