package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import com.server.jd.domain.JobDescription;
import com.server.resume.domain.Resume;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JD 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JobDescription jobDescription;

    // Resume 연관관계
    // 하나의 이력서에 1차, 2차 인터뷰가 생길 수 있으니 ManyToOne으로 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    // Organizer (생성자, 주최자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private LocalDateTime scheduledAt; // 예정 면접 시간

    // 인터뷰 진행현황 (WAITING, IN_PROGRESS, DONE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;

    //AI가 생성한 면접 요약 결과 저장 필드
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewParticipant> interviewParticipant = new ArrayList<>();

    public static Interview of(
            JobDescription jobDescription,
           Resume resume,
           User organizer,
           LocalDateTime scheduledAt,
           InterviewStatus status
    ) {
        Interview interview = new Interview();
        interview.jobDescription = jobDescription;
        interview.resume = resume;
        interview.organizer = organizer;
        interview.scheduledAt = scheduledAt;
        interview.status = status;
        return interview;
    }

    //인터뷰 요약 저장 메서드

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}