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

    @Column(name = "gender", length = 10)
    private String gender; // DB에는 String으로 저장

    @Column(name = "company_name")
    private String companyName;

    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.UNVERIFIED;

    @Column(name = "email_token", unique = true)
    private String emailToken;

    @Column(name = "email_expiry")
    private LocalDateTime emailExpiry;

    // Enum 으로 꺼내 쓰고 싶을 때
    public Gender getGenderEnum() {
        if (this.gender == null) {
            return null;
        }
        try {
            return Gender.valueOf(this.gender);  // "MALE" -> Gender.MALE
        } catch (IllegalArgumentException e) {
            // DB에 이상한 값이 들어있을 때
            return null;
        }
    }

    // Enum 으로 세팅하고 싶을 때
    public void setGenderEnum(Gender gender) {
        this.gender = (gender != null) ? gender.name() : null; // Gender.MALE -> "MALE"
    }



    public static User of(String email,
                          String password,
                          String name,
                          String nickname,
                          String phoneNumber,
                          LocalDate birthDate,
                          Gender gender,
                          String companyName,
                          String position) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.nickname = nickname;
        user.phoneNumber = phoneNumber;
        user.birthDate = birthDate;
        user.setGenderEnum(gender);
        user.companyName = companyName;
        user.position = position;
        user.status = EmailStatus.UNVERIFIED;
        return user;
    }

    // 마이페이지 정보 수정

    public void updateProfile(
            String name,
            String nickname,
            String position,
            String phoneNumber,
            LocalDate birthDate,
            Gender gender
    ) {
        this.name = name;
        this.nickname = nickname;
        this.position = position;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.setGenderEnum(gender);
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

    // 비밀번호 변경
    public void changePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }
    //이메일 인증 완료
    public void markEmailVerified() {
        this.status = EmailStatus.VERIFIED;
    }
}