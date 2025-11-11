package com.server.jd.domain;

import com.server.global.entity.BaseEntity;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="job_descriptions")
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

    @ElementCollection
    @CollectionTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_description_id"))
    @Column(name = "skill")
    private List<String> requiredSkills;

    @ElementCollection
    @CollectionTable(name = "job_preferred_skills", joinColumns = @JoinColumn(name = "job_description_id"))
    @Column(name = "skill")
    private List<String> preferredSkills;

    @Lob
    private String welfare;

    private Long applicantCount;

    // 작성자 (User 엔티티 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


    public static JobDescription of(String title,
                                    String department,
                                    String workType,
                                    String experience,
                                    String education,
                                    String salary,
                                    String description,
                                    LocalDate startDate,
                                    LocalDate deadline,
                                    JobStatus status,
                                    List<String> requiredSkills,
                                    List<String> preferredSkills,
                                    String welfare,
                                    Long applicantCount,
                                    User author) {

        JobDescription jd = new JobDescription();
        jd.title = title;
        jd.department = department;
        jd.workType = workType;
        jd.experience = experience;
        jd.education = education;
        jd.salary = salary;
        jd.description = description;
        jd.startDate = startDate;
        jd.deadline = deadline;
        jd.status = status;
        jd.requiredSkills = requiredSkills;
        jd.preferredSkills = preferredSkills;
        jd.welfare = welfare;
        jd.applicantCount = applicantCount;
        jd.author = author;
        return jd;
    }
}