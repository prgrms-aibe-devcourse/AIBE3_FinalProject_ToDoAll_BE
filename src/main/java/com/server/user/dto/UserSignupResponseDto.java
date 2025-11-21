package com.server.user.dto;


import com.server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupResponseDto {

    private Long id;            // 생성된 유저 ID
    private String email;       // 이메일
    private String name;        // 이름
    private String nickname;    // 닉네임
    private String companyName; //기업명
    private String position;    //직책/직무
    private LocalDateTime createdAt;

    public static UserSignupResponseDto from(User user) {
        return new UserSignupResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getCompanyName(),
                user.getPosition(),
                user.getCreatedAt()
        );

    }
}
