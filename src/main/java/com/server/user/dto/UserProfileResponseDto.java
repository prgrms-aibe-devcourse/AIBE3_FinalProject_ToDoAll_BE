package com.server.user.dto;

import com.server.user.domain.Gender;
import com.server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {

    private Long id;               // 사용자 ID
    private String email;          // 이메일(로그인 ID)
    private String name;           // 이름
    private String nickname;       // 닉네임
    private String companyName;    // 회사명
    private String position;       // 직책/직무
    private String phoneNumber;    // 전화번호
    private LocalDate birthDate;   // 생년월일
    private Gender gender;         //성별
    private String profileUrl;     // 프로필 이미지 URL (기본 이미지 포함)


    public static UserProfileResponseDto from(User user, String resolvedProfileUrl) {

        return new UserProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getCompanyName(),
                user.getPosition(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getGender(),
                resolvedProfileUrl
        );
    }
}
