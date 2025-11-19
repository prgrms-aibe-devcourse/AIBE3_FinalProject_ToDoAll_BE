package com.server.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequestDto {

    @NotBlank(message = "이메일 인증은 필수입니다.")
    private String email; // 회사 이메일

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "비밀번호는 영문과 숫자를 포함해야 합니다"
    )
    private String password; // 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm; // 비밀번호 확인

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")

    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9_]+$",
            message = "닉네임은 한글, 영문, 숫자, 언더스코어만 사용 가능합니다"
    )
    private String nickname;

    @Size(max = 50, message = "회사명은 50자 이하여야 합니다")
    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;

    @Size(max = 50, message = "직책은 50자 이하여야 합니다")
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
