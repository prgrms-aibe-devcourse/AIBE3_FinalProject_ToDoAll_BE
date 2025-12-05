package com.server.admin.dto;

import com.server.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecentUserDto {

    private final Long id;
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;

    private RecentUserDto(Long id, String email, String name, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static RecentUserDto from(User user) {
        return new RecentUserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt()
        );
    }
}

