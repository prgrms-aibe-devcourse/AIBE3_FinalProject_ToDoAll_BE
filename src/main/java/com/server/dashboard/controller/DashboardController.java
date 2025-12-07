package com.server.dashboard.controller;

import com.server.dashboard.dto.*;
import com.server.dashboard.service.DashboardService;

import com.server.dashboard.type.CalendarEventType;
import com.server.dashboard.type.CustomDayOfWeek;
import com.server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.server.dashboard.util.Formatter.formatterTimeWithAMPM;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard API", description = "대시보드 API 컨트롤러")
public class DashboardController {
    private final DashboardService dashboardService;

    //TODO : 인가 로직 작성
    @GetMapping("/summary/active")
    @Operation(summary = "요약카드 - 활성 공고", description = "모집 중인 채용 공고 수")
    public CommonResponse<Integer> showActiveJobs () {
        return CommonResponse.success(dashboardService.getActiveJobsCount());
    }

    @GetMapping("/summary/applicant")
    @Operation(summary = "요약카드 - 총 지원자", description = "모집 중인 공고의 전체 지원자 수")
    public CommonResponse<Long> showAllApplicants () {
        return CommonResponse.success(dashboardService.getAllApplicantsByJobStatus());
    }

    @GetMapping("/summary/interview")
    @Operation(summary = "요약카드 - 예정된 면접", description = "7일 내 다가오는 면접")
    public CommonResponse<Integer> showScheduledActiveJobs () {
        return CommonResponse.success(dashboardService.getWeekInterviewsCount());
    }

    @GetMapping("/summary/hired")
    @Operation(summary = "요약카드 - 채용 완료", description = "이번 달 합격자 수")
    public CommonResponse<Long> showHired() {
        return CommonResponse.success(dashboardService.getMonthHiredCount());
    }

    @GetMapping("/detail/job-result")
    @Operation(summary = "상세 - 공고별 지원자 현황", description = "각 채용 공고 별 지원자 현황")
    public CommonResponse<List<DashboardDetailJobResultResponseDto>> showJobResult() {
        return CommonResponse.success(dashboardService.getApplicantStatusForEachJob());
    }

    @GetMapping("/detail/upcoming-interview")
    @Operation(summary = "상세 - 다가오는 면접", description = "이번 주 예정된 면접 일정")
    public CommonResponse<List<DashboardUpcomingInterviewResponseDto>> showUpcomingInterview() {
        return CommonResponse.success(dashboardService.getUpComingInterviews());
    }

    @GetMapping("/detail/job-status")
    @Operation(summary = "상세 - 공고 현황", description = "채용 공고의 상태별 현황")
    public CommonResponse<DashboardNumByProgressStatusResponseDto> showJobStatus() {
        return CommonResponse.success(dashboardService.getCountByJobStatus());
    }

    @GetMapping("/detail/interview-status")
    @Operation(summary = "상세 - 면접 현황", description = "면접 진행 상태별 현황")
    public CommonResponse<DashboardNumByProgressStatusResponseDto> showInterviewStatus() {
        return CommonResponse.success(dashboardService.getCountByInterviewStatus());
    }

    @GetMapping("/week-calendar")
    @Operation(summary = "이번 주 캘린더", description = "이번 주 스케쥴 요약")
    public CommonResponse<DashboardWeeklyCalendarResponseDto> showWeekCalendar() {
        return CommonResponse.success(dashboardService.getWeekCalendarData());
    }
}
