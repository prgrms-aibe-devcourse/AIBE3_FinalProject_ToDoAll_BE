package com.server.dashboard.controller;

import com.server.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "DashboardController", description = "대시보드 API 컨트롤러")
public class DashboardController {
    private final DashboardService dashboardService;


    @GetMapping("/summation/active")
    @Operation(summary = "요약카드 - 활성 공고", description = "진행 중인 채용 공고 수")
    public String showActiveJobs () {
        return "";
    }

    @GetMapping("/summation/applicant")
    @Operation(summary = "요약카드 - 총 지원자", description = "전체 지원자 수")
    public String showAllApplicants () {
        return "";
    }

    @GetMapping("/summation/interview")
    @Operation(summary = "요약카드 - 예정된 면접", description = "이번 주 예정된 면접 수")
    public String showScheduledActiveJobs () {
        return "";
    }

    @GetMapping("/summation/hired")
    @Operation(summary = "요약카드 - 채용 완료", description = "이번 달 채용 완료 수")
    public String showHired() {
        return "";
    }

    @GetMapping("/detail/job-result")
    @Operation(summary = "상세 - 공고별 합격 현황", description = "각 채용 공고의 진행 상황")
    public String showJobResult() {
        return "";
    }

    @GetMapping("/detail/upcoming-interview")
    @Operation(summary = "상세 - 다가오는 면접", description = "이번 주 예정된 면접 일정")
    public String showUpcomingInterview() {
        return "";
    }

    @GetMapping("/detail/job-status")
    @Operation(summary = "상세 - 공고 현황", description = "채용 공고의 상태별 현황")
    public String showJobStatus() {
        return "";
    }

    @GetMapping("/detail/interview-status")
    @Operation(summary = "상세 - 면접 현황", description = "면접 진행 상태별 현황")
    public String showInterviewStatus() {
        return "";
    }

    @GetMapping("/week-calendar")
    @Operation(summary = "상세 - 면접 현황", description = "면접 진행 상태별 현황")
    public String showWeekCalendar() {
        return "";
    }

}
