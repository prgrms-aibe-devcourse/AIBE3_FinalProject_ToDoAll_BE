package com.server.user.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    public static User of(String email,
                          String password,
                          String name,
                          String nickname,
                          String phoneNumber,
                          LocalDate birthDate,
                          String gender,
                          String companyName) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.nickname = nickname;
        user.phoneNumber = phoneNumber;
        user.birthDate = birthDate;
        user.gender = gender;
        user.companyName = companyName;
        return user;
    }
}