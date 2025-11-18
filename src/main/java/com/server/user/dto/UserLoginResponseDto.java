package com.server.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponseDto {
    private String accessToken;
    private String refreshToken;

    public static UserLoginResponseDto of(String accessToken, String refreshToken) {
        return new UserLoginResponseDto(accessToken, refreshToken);
    }
}
