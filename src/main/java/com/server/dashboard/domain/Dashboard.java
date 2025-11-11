package com.server.dashboard.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dashboard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer totalJobs;             // 총 채용공고 수
    private Integer totalActiveJobs;       // 총 활성공고 수
    private Integer totalResumes;          // 총 등록 이력서 수
    private Integer totalActiveResumes;    // 활성공고에 지원된 이력서 수
    private Integer totalMatches;          // 총 매칭 수
    private Integer totalInterviews;       // 총 면접 수
    private Integer completedInterviews;   // 완료된 면접 수
    private Integer completedHires;        // 최종 채용 완료 수
    private Integer upcomingInterviews;    // 다가오는 면접 수

    public static Dashboard of(
            Integer totalJobs,
            Integer totalActiveJobs,
            Integer totalResumes,
            Integer totalActiveResumes,
            Integer totalMatches,
            Integer totalInterviews,
            Integer completedInterviews,
            Integer completedHires,
            Integer upcomingInterviews
    ) {
        Dashboard dashboard = new Dashboard();
        dashboard.totalJobs = totalJobs;
        dashboard.totalActiveJobs = totalActiveJobs;
        dashboard.totalResumes = totalResumes;
        dashboard.totalActiveResumes = totalActiveResumes;
        dashboard.totalMatches = totalMatches;
        dashboard.totalInterviews = totalInterviews;
        dashboard.completedInterviews = completedInterviews;
        dashboard.completedHires = completedHires;
        dashboard.upcomingInterviews = upcomingInterviews;
        return dashboard;
    }
}