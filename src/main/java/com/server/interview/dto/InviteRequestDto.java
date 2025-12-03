package com.server.interview.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteRequestDto {

    @NotBlank
    @Email
    private String applicantEmail; // 지원자 이메일

    @NotBlank
    private String applicantName; // 지원자 이름
}
