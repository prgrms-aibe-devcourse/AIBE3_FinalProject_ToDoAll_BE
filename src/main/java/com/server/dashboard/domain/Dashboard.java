package com.server.dashboard.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Dashboard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 총 채용공고 수
    private Integer totalJobs;

    // 총 이력서 수
    private Integer totalResumes;

    // 총 매칭 수
    private Integer totalMatches;

    // 총 면접 수
    private Integer totalInterviews;

    // 완료된 면접 수
    private Integer completedInterviews;

    // 최종 채용 완료 수
    private Integer completedHires;

    // 다가오는 면접 수
    private Integer upcomingInterviews;
}
