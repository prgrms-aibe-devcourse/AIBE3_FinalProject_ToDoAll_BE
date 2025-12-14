package com.server.user.dto;

import com.server.user.domain.User;

public record UsersByEmailDomainResponseDto(
        Long id,
        String name,
        String email,
        String avatar
) {
    public static UsersByEmailDomainResponseDto of(
            User user,
            String avatarUrl
    ) {
        return new UsersByEmailDomainResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                avatarUrl
        );
    }
}

