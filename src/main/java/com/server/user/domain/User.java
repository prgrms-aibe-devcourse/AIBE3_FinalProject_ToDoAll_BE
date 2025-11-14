package com.server.user.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private String name;

    private String nickname;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String gender;

    @Column(name = "company_name")
    private String companyName;

    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    @Column(name = "email_token", unique = true)
    private String emailToken;

    @Column(name = "email_expiry")
    private LocalDateTime emailExpiry;

    public static User of(String email,
                          String password,
                          String name,
                          String nickname,
                          String phoneNumber,
                          LocalDate birthDate,
                          String gender,
                          String companyName,
                          String position) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.nickname = nickname;
        user.phoneNumber = phoneNumber;
        user.birthDate = birthDate;
        user.gender = gender;
        user.companyName = companyName;
        user.position = position;
        user.status = EmailStatus.UNVERIFIED;
        return user;
    }
    // 회원가입 전용 메서드
    public static User createForSignup(
            String email,
            String encodedPassword,
            String name,
            String nickname,
            String companyName,
            String position
    ) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.companyName = companyName;
        user.position = position;
        user.status = EmailStatus.VERIFIED;
        return user;
    }

    // 마이페이지에서 추가/수정할 수 있는 프로필 정보 업데이트
    public void updateProfile(String phoneNumber, LocalDate birthDate, String gender) {
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    // 비밀번호 변경
    public void changePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }
}