package com.server.dashboard.controller;

import com.server.dashboard.dto.*;
import com.server.dashboard.service.DashboardService;

import com.server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard API", description = "대시보드 API 컨트롤러")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary/actives")
    @Operation(summary = "요약카드 - 활성 공고", description = "모집 중인 채용 공고 수")
    public CommonResponse<Integer> showActiveJobs (
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getActiveJobsCount(userId));
    }

    @GetMapping("/summary/applicants")
    @Operation(summary = "요약카드 - 총 지원자", description = "모집 중인 공고의 전체 지원자 수")
    public CommonResponse<Long> showAllApplicants (
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getApplicantsCountOfActiveJobs(userId));
    }

    @GetMapping("/summary/interviews")
    @Operation(summary = "요약카드 - 예정된 면접", description = "7일 내 다가오는 면접")
    public CommonResponse<Integer> showScheduledInterviews (
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getScheduledInterviewsCount(userId));
    }

    @GetMapping("/summary/hires")
    @Operation(summary = "요약카드 - 채용 완료", description = "이번 달 합격자 수")
    public CommonResponse<Long> showHired(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getMonthHiredCount(userId));
    }

    @GetMapping("/detail/applicant-stat-byJob")
    @Operation(summary = "상세 - 공고별 지원자 현황", description = "각 채용 공고 별 지원자 현황")
    public CommonResponse<List<DashboardApplicantStatsResponseDto>> showJobResult(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getApplicantStatsForEachJob(userId));
    }

    @GetMapping("/detail/upcoming-interview")
    @Operation(summary = "상세 - 다가오는 면접", description = "7일 내 예정된 면접 일정")
    public CommonResponse<List<DashboardUpcomingInterviewsResponseDto>> showUpcomingInterviews(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getUpComingInterviews(userId));
    }

    @GetMapping("/detail/job-status")
    @Operation(summary = "상세 - 공고 현황", description = "채용 공고의 상태별 현황")
    public CommonResponse<DashboardJobStatusResponseDto> showJobStatus(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getCountByJobStatus(userId));
    }

    @GetMapping("/detail/interview-status")
    @Operation(summary = "상세 - 면접 현황", description = "면접 진행 상태별 현황")
    public CommonResponse<DashboardJobStatusResponseDto> showInterviewStatus(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getCountByInterviewStatus(userId));
    }

    @GetMapping("/week-calendar")
    @Operation(summary = "이번 주 캘린더", description = "이번 주 스케쥴 요약")
    public CommonResponse<DashboardWeeklyCalendarResponseDto> showWeekCalendar(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(dashboardService.getWeekCalendarData(userId));
    }
}
