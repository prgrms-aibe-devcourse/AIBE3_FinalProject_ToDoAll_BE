package com.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailAuthSendRequestDto {

    @NotBlank(message = "이메일은 필수 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private EmailAuthSendRequestDto(String email) {
        this.email = email;
    }

    public static EmailAuthSendRequestDto of(String email) {
        return new EmailAuthSendRequestDto(email);
    }
}
