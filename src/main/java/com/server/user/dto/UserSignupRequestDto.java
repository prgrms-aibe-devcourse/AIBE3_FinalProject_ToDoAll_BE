package com.server.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequestDto {


    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email; // 회사 이메일

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            message = "비밀번호는 8~64자, 영문·숫자를 포함해야 합니다."
    )
    private String password; // 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm; // 비밀번호 확인

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;

    @NotBlank(message = "직책/직무는 필수입니다.")
    private String position;

    public static UserSignupRequestDto of(
            String email,
            String password,
            String passwordConfirm,
            String name,
            String nickname,
            String companyName,
            String position
    ) {
        UserSignupRequestDto req = new UserSignupRequestDto();
        req.email = email;
        req.password = password;
        req.passwordConfirm = passwordConfirm;
        req.name = name;
        req.nickname = nickname;
        req.companyName = companyName;
        req.position = position;
        return req;
    }

}
