package com.server.user.dto;

import com.server.user.domain.Gender;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 마이페이지 - 내 정보 수정 요청

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequestDto {

    // 이름 (필수 여부는 정책에 따라, 일단 길이만 제한)
    @Size(max = 50, message = "이름은 최대 50자까지 가능합니다.")
    private String name;

    // 닉네임
    @Size(max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    private String nickname;

    // 직책/직무
    @Size(max = 50, message = "직책은 최대 50자까지 가능합니다.")
    private String position;

    // 전화번호
    @Size(max = 20, message = "전화번호는 최대 20자까지 가능합니다.")
    private String phoneNumber;

    // 생년월일 (yyyy-MM-dd)
    @PastOrPresent(message = "생년월일은 오늘보다 미래일 수 없습니다.")
    private LocalDate birthDate;

    private Gender gender;

}
