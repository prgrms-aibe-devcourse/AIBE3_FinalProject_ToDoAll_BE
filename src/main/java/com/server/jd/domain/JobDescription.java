package com.server.jd.domain;

import com.server.global.entity.BaseEntity;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}