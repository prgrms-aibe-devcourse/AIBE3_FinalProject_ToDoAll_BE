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
}