package com.server.match.domain;

import com.server.global.entity.BaseEntity;
import com.server.jd.domain.JobDescription;
import com.server.resume.domain.Resume;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "matches")
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JD 연관관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JobDescription jobDescription;

    // Resume 연관관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private LocalDateTime appliedAt; // 지원 일시

    private Float matchScore; // 매칭 점수

    @Lob
    private String recommendationReason; // 추천 사유

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    public static Match of(JobDescription jobDescription,
                           Resume resume,
                           LocalDateTime appliedAt,
                           Float matchScore,
                           String recommendationReason,
                           MatchStatus status) {
        Match match = new Match();
        match.jobDescription = jobDescription;
        match.resume = resume;
        match.appliedAt = appliedAt;
        match.matchScore = matchScore;
        match.recommendationReason = recommendationReason;
        match.status = status;
        return match;
    }
}
