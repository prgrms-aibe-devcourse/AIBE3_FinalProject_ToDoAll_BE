package com.server.resume.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resumes")
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String email;

    private String phone;

    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "resume_file_url")
    private String resumeFileUrl;

    @Column(name = "portfolio_file_url")
    private String portfolioFileUrl;

    @Enumerated(EnumType.STRING)
    private ResumeStatus status;

    public static Resume of(String name,
                            String gender,
                            LocalDate birthDate,
                            String email,
                            String phone,
                            String address,
                            String detailAddress,
                            String resumeFileUrl,
                            String portfolioFileUrl,
                            ResumeStatus status) {
        Resume resume = new Resume();
        resume.name = name;
        resume.gender = gender;
        resume.birthDate = birthDate;
        resume.email = email;
        resume.phone = phone;
        resume.address = address;
        resume.detailAddress = detailAddress;
        resume.resumeFileUrl = resumeFileUrl;
        resume.portfolioFileUrl = portfolioFileUrl;
        resume.status = status;
        return resume;
    }
}