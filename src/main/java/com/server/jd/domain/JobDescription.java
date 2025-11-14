package com.server.jd.domain;

import com.server.global.entity.BaseEntity;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "job_descriptions")
public class JobDescription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공고 제목
    @Column(nullable = false)
    private String title;

    private String department;

    private String workType;

    private String experience;

    private String education;

    private String salary;

    @Lob
    private String description;

    private LocalDate startDate;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Lob
    private String welfare;

    private String location;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(nullable = false)
    private long applicantCount = 0; // 기본값 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    public static JobDescription of(
            String title,
            String department,
            String workType,
            String experience,
            String education,
            String salary,
            String description,
            LocalDate startDate,
            LocalDate deadline,
            JobStatus status,
            String welfare,
            Long applicantCount,
            String location,
            String thumbnailUrl,
            User author
    ) {
        JobDescription jd = new JobDescription();
        jd.title = title;
        jd.department = department;
        jd.workType = workType;
        jd.experience = experience;
        jd.education = education;
        jd.salary = salary;
        jd.description = description;
        jd.startDate = LocalDate.now();
        jd.deadline = deadline;
        jd.status = status;
        jd.welfare = welfare;
        jd.applicantCount = applicantCount;
        jd.location = location;
        jd.thumbnailUrl = thumbnailUrl;
        jd.author = author;
        return jd;
    }

    // 지원자 수 증가 메서드
    public void increaseApplicantCount() {
        this.applicantCount++;
    }
}