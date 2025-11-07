package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InterviewEvaluation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평가 대상 면접
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    // 평가자 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    // 기술 점수
    private Integer scoreTech;

    // 커뮤니케이션 점수
    private Integer scoreComm;

    // 종합 점수
    private Integer scoreOverall;

    // 평가 코멘트
    @Column(columnDefinition = "TEXT")
    private String comment;

    // 면접 결과 (PASS, HOLD, FAIL)
    @Enumerated(EnumType.STRING)
    private InterviewResult result;
}
