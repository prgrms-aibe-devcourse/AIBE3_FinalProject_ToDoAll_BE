package com.server.user.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class User extends BaseEntity {

    // 예시입니다. 유저 관련 각 필드는 ERD를 참고해서 본인이 수정해주세요.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private String name;

    private String nickname;

    private String phoneNumber;

    private LocalDate birthDate;
}
