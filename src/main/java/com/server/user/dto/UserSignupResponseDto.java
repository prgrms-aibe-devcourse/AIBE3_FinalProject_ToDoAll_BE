package com.server.user.dto;


import com.server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupResponseDto {

    private Long id;          // 생성된 유저 ID
    private String email;     // 이메일
    private String name;      // 이름
    private String nickname;  // 닉네임

    public static UserSignupResponseDto from(User user) {
        return new UserSignupResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname()
        );
    }
}
