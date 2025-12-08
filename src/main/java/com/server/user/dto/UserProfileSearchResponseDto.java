package com.server.user.dto;

import com.server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSearchResponseDto {

    private Long id;               // 사용자 ID
    private String name;           // 이름
    private String nickname;       // 닉네임
    private String profileUrl;     // 프로필 이미지 URL (기본 이미지 포함)


    public static UserProfileSearchResponseDto from(User user, String resolvedProfileUrl) {

        return new UserProfileSearchResponseDto(
                user.getId(),
                user.getName(),
                user.getNickname(),
                resolvedProfileUrl
        );
    }
}
