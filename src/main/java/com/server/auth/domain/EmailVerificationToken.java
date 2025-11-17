package com.server.auth.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

 //이메일 인증 토큰 엔티티
@Entity
@Table(name = "email_verification_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime verifiedAt;

    //이메일 인증 토큰 생성

    public static EmailVerificationToken createToken(String email, int expiryMinutes) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.email = email;
        token.token = UUID.randomUUID().toString();
        token.expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        return token;
    }


    //토큰 만료 여부 확인

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }


    //이미 인증됨 여부 확인

    public boolean isVerified() {
        return verifiedAt != null;
    }

    //인증 완료 처리

    public void markAsVerified() {
        this.verifiedAt = LocalDateTime.now();
    }
}
