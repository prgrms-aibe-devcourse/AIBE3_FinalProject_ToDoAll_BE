package com.server.resume.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String gender;

    private LocalDate birthDate;

    private String email;

    private String phone;

    private String address;

    private String detailAddress;

    @ElementCollection
    @CollectionTable(name = "resume_education", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> education;

    @ElementCollection
    @CollectionTable(name = "resume_experience", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> experience;

    @ElementCollection
    @CollectionTable(name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> skills;

    @ElementCollection
    @CollectionTable(name = "resume_activities", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> activities;

    @ElementCollection
    @CollectionTable(name = "resume_certifications", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> certifications;

    private String resumeFileUrl;

    private String portfolioFileUrl;

    @Enumerated(EnumType.STRING)
    private ResumeStatus status;
}
