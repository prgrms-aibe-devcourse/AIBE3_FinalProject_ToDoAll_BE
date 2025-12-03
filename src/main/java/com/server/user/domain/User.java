package com.server.user.domain;


import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "email_domain")
    private String emailDomain;

    private String password;

    private String name;

    private String nickname;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;


    @Column(name = "company_name")
    private String companyName;

    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.UNVERIFIED; //이메일 인증 상태

    @Column(name = "email_token", unique = true)
    private String emailToken;

    // 프로필 이미지 URL
    @Column(name = "profile_url")
    private String profileUrl;

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
        this.gender = gender;
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
        user.emailDomain = extractEmailDomain(email);      // 도메인 자동 세팅
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.companyName = companyName;
        user.position = position;
        user.status = EmailStatus.VERIFIED;
        return user;
    }

    // 이메일에서 도메인(@ 뒤)만 추출하는 유틸 메서드
    private static String extractEmailDomain(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf("@");
        if (atIndex < 0 || atIndex == email.length() - 1) return null;
        return email.substring(atIndex + 1);
    }

    // 정적 팩토리 메서드 (범용 User 생성용)
    public static User of(
            String email,
            String encodedPassword,
            String name,
            String nickname,
            String phoneNumber,
            LocalDate birthDate,
            Gender gender,              // ← enum Gender로 받기
            String companyName,
            String position
    ) {
        User user = new User();
        user.email = email;
        user.emailDomain = extractEmailDomain(email);  // ← 자동 도메인 저장
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.phoneNumber = phoneNumber;
        user.birthDate = birthDate;
        user.gender = gender;
        user.companyName = companyName;
        user.position = position;
        user.status = EmailStatus.UNVERIFIED; // 기본값: 미인증
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

    // 프로필 이미지 변경 (S3 업로드 후 URL 저장)
    public void changeProfileImage(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    //프로필 이미지 URL 조회

    public String getProfileImageOrDefault(String defaultUrl) {
        if (this.profileUrl == null || this.profileUrl.isBlank()) {
            return defaultUrl;
        }
        return this.profileUrl;
    }
}