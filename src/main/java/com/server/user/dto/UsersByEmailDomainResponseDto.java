package com.server.user.dto;

import com.server.user.domain.User;

public record UsersByEmailDomainResponseDto (
        Long id,
        String name,
        String email,
        String avatar
){
    public static UsersByEmailDomainResponseDto from(User user) {
        return new UsersByEmailDomainResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileUrl() // 없다면 기본값 세팅 가능
        );
    }
}
