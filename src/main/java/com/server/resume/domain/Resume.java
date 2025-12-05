package com.server.resume.domain;

import com.server.global.entity.BaseEntity;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.Skill;
import com.server.resume.exception.ResumeErrorCase;
import com.server.search.document.ResumeDocument;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resumes")
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jd_id", nullable = false)
    private JobDescription jobDescription;

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

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeEducation> educations = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<ResumeExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<ResumeSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<ResumeActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<ResumeCertification> certifications = new ArrayList<>();

    @Lob
    @Column
    private String memo;


    public static Resume of(JobDescription jobDescription,
                            String name,
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
        resume.jobDescription = jobDescription;
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


    public void addEducation(EducationLevel educationLevel,
                             String schoolName,
                             String major,
                             Boolean isGraduated,
                             LocalDate admissionDate,
                             LocalDate graduationDate,
                             AttendanceType attendanceType,
                             Double gpa,
                             Double gpaScale) {
        if(gpa != null && gpa < 0) {

        }
        if(gpaScale != null && gpaScale < 0) {

        }
        ResumeEducation edu = ResumeEducation.of(this, educationLevel, schoolName, major, isGraduated, admissionDate, graduationDate, attendanceType, gpa, gpaScale);
        this.educations.add(edu);
    }

    public void addExperience(String companyName,
                              String department,
                              String position,
                              LocalDate startDate,
                              LocalDate endDate) {
        ResumeExperience exp = ResumeExperience.of(this, companyName, department, position, startDate, endDate);
        this.experiences.add(exp);
    }

    public void addSkill(Skill skill, ProficiencyLevel proficiencyLevel) {
        ResumeSkill rs = ResumeSkill.of(this, skill, proficiencyLevel);
        this.skills.add(rs);
    }

    public void addActivity(String title,
                            ResumeActivityType type,
                            String organization
                            ) {
        ResumeActivity activity = ResumeActivity.of(this, title, type, organization);
        this.activities.add(activity);
    }

    public void addCertification(ResumeCertificationType type, String name, String scoreOrLevel) {
        ResumeCertification cert = ResumeCertification.of(this, type, name, scoreOrLevel);
        this.certifications.add(cert);
    }

    public void updateStatus(ResumeStatus newStatus) {
        if (newStatus == null) {
            throw new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND);
        }
        this.status = newStatus;
    }

    public static Resume createFromDocument(ResumeDocument doc) {
        Resume resume = new Resume();
        resume.name = doc.getName();
        resume.gender = doc.getGender();
        resume.birthDate = doc.getBirthDate();
        resume.email = doc.getEmail();
        resume.phone = doc.getPhone();
        resume.address = doc.getAddress();
        resume.detailAddress = doc.getDetailAddress();
        resume.resumeFileUrl = doc.getResumeFileUrl();
        resume.portfolioFileUrl = doc.getPortfolioFileUrl();
        resume.status = ResumeStatus.NEW;
        return resume;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateResumeFileKey(String fileKey) {
        this.resumeFileUrl = fileKey;
    }

    public void updatePortfolioFileKey(String fileKey) {
        this.portfolioFileUrl = fileKey;
    }
}