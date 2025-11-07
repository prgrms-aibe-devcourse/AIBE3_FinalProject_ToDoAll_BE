package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import com.server.jd.domain.JobDescription;
import com.server.resume.domain.Resume;
import jakarta.persistence.*;
import lombok.*;
import com.server.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JD 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JobDescription jobDescription;

    // Resume 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    // Organizer (생성자, 주최자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private LocalDateTime scheduledAt; // 예정 면접 시간

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    @Lob
    private String summary; // 면접 요약
}