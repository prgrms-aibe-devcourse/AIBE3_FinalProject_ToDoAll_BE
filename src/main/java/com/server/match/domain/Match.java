package com.server.match.domain;

import com.server.global.entity.BaseEntity;
import com.server.jd.domain.JobDescription;
import com.server.resume.domain.Resume;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "matches")
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JD 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JobDescription jobDescription;

    // 이력서 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private LocalDateTime appliedAt;

    private Float matchScore;

    @Lob
    private String recommendationReason;

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

    public void updateStatus(MatchStatus newStatus) {
        this.status = newStatus;
    }
}