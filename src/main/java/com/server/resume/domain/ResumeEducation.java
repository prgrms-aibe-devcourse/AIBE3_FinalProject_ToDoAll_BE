package com.server.resume.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resume_educations")
public class ResumeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "education_level")
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @Column(name = "school_name")
    private String schoolName;

    private String major;

    @Column(name = "is_graduated")
    private Boolean isGraduated;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "attendance_type")
    @Enumerated(EnumType.STRING)
    private AttendanceType attendanceType; // 주간, 야간 등

    private Double gpa; // 학점

    @Column(name = "gpa_scale")
    private Double gpaScale; // 기준 학점

    public static ResumeEducation of(Resume resume,
                                     EducationLevel educationLevel,
                                     String schoolName,
                                     String major,
                                     Boolean isGraduated,
                                     LocalDate admissionDate,
                                     LocalDate graduationDate,
                                     AttendanceType attendanceType,
                                     Double gpa,
                                     Double gpaScale) {
        ResumeEducation edu = new ResumeEducation();
        edu.resume = resume;
        edu.educationLevel = educationLevel;
        edu.schoolName = schoolName;
        edu.major = major;
        edu.isGraduated = isGraduated;
        edu.admissionDate = admissionDate;
        edu.graduationDate = graduationDate;
        edu.attendanceType = attendanceType;
        edu.gpa = gpa;
        edu.gpaScale = gpaScale;
        return edu;
    }
}
