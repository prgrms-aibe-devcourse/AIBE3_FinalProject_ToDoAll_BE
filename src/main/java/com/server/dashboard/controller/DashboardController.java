package com.server.dashboard.controller;

import com.server.dashboard.dto.*;
import com.server.dashboard.service.DashboardService;

import com.server.dashboard.type.CalendarEventType;
import com.server.dashboard.type.JobStatusOfProgress;
import com.server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard API", description = "대시보드 API 컨트롤러")
public class DashboardController {
    private final DashboardService dashboardService;


    @GetMapping("/summary/active")
    @Operation(summary = "요약카드 - 활성 공고", description = "진행 중인 채용 공고 수")
    public CommonResponse<Integer> showActiveJobs () {
        //Todo : 비즈니스 로직 작성
        int value = 12;

        return CommonResponse.success(value);
    }

    @GetMapping("/summary/applicant")
    @Operation(summary = "요약카드 - 총 지원자", description = "전체 지원자 수")
    public CommonResponse<Integer> showAllApplicants () {
        //Todo : 비즈니스 로직 작성
        int value = 9;

        return CommonResponse.success(value);
    }

    @GetMapping("/summary/interview")
    @Operation(summary = "요약카드 - 예정된 면접", description = "이번 주 예정된 면접 수")
    public CommonResponse<Integer> showScheduledActiveJobs () {
        //Todo : 비즈니스 로직 작성
        int value = 18;
        return CommonResponse.success(value);
    }

    @GetMapping("/summary/hired")
    @Operation(summary = "요약카드 - 채용 완료", description = "이번 달 채용 완료 수")
    public CommonResponse<Integer> showHired() {
        //Todo : 비즈니스 로직 작성
        int value = 20;
        return CommonResponse.success(value);
    }

    @GetMapping("/detail/job-result")
    @Operation(summary = "상세 - 공고별 합격 현황", description = "각 채용 공고의 진행 상황")
    public CommonResponse<ArrayList<DashboardDetailJobResultResponseDto>> showJobResult() {
        ArrayList<DashboardDetailJobResultResponseDto> detailJobResults = new ArrayList<>();
        //Todo : 비즈니스 로직 작성

        detailJobResults.add(new DashboardDetailJobResultResponseDto("시니어 프론트엔드 개발자",  new ArrayList<>(List.of(1, 3, 4, 3)), JobStatusOfProgress.DOCUMENT));
        detailJobResults.add(new DashboardDetailJobResultResponseDto("백엔드 개발자",  new ArrayList<>(List.of(1, 3, 4, 3)), JobStatusOfProgress.DOCUMENT));
        detailJobResults.add(new DashboardDetailJobResultResponseDto("주니어 풀스택 개발자",  new ArrayList<>(List.of(1, 3, 4, 3)), JobStatusOfProgress.DOCUMENT));

        return CommonResponse.success(detailJobResults);
    }

    @GetMapping("/detail/upcoming-interview")
    @Operation(summary = "상세 - 다가오는 면접", description = "이번 주 예정된 면접 일정")
    public CommonResponse<ArrayList<DashboardUpcomingInterviewResponseDto>> showUpcomingInterview() {
        //Todo : 비즈니스 로직 작성
        ArrayList<DashboardUpcomingInterviewResponseDto> upcomingInterviews = new ArrayList<>();
        upcomingInterviews.add(DashboardUpcomingInterviewResponseDto.from(
                LocalDateTime.now(),
                "김상진",
                "시니어 프론트엔드 개발자",
                new ArrayList<>(List.of("일접관, 이접관"))
        ));
        upcomingInterviews.add(DashboardUpcomingInterviewResponseDto.from(
                LocalDateTime.now(),
                "김하진",
                "백엔드 개발자",
                new ArrayList<>(List.of("삼접관, 사접관"))
        ));
        upcomingInterviews.add(DashboardUpcomingInterviewResponseDto.from(
                LocalDateTime.now(),
                "김중진",
                "시니어 프론트엔드 개발자",
                new ArrayList<>(List.of("오접관, 육접관"))
        ));
        upcomingInterviews.add(DashboardUpcomingInterviewResponseDto.from(
                LocalDateTime.now(),
                "김일진",
                "시니어 프론트엔드 개발자",
                new ArrayList<>(List.of("칠접관, 팔접관"))
        ));
        upcomingInterviews.add(DashboardUpcomingInterviewResponseDto.from(
                LocalDateTime.now(),
                "김희진",
                "주니어 풀스택 개발자",
                new ArrayList<>(List.of("구접관, 십접관"))
        ));

        return CommonResponse.success(upcomingInterviews);
    }

    @GetMapping("/detail/job-status")
    @Operation(summary = "상세 - 공고 현황", description = "채용 공고의 상태별 현황")
    public CommonResponse<DashboardNumByProgressStatusResponseDto> showJobStatus() {
        //Todo : 비즈니스 로직 작성
        return CommonResponse.success(new DashboardNumByProgressStatusResponseDto(5, 6, 11));
    }

    @GetMapping("/detail/interview-status")
    @Operation(summary = "상세 - 면접 현황", description = "면접 진행 상태별 현황")
    public CommonResponse<DashboardNumByProgressStatusResponseDto> showInterviewStatus() {
        //Todo : 비즈니스 로직 작성
        return CommonResponse.success(new DashboardNumByProgressStatusResponseDto(20, 2, 7));
    }

    @GetMapping("/week-calendar")
    @Operation(summary = "이번 주 캘린더", description = "이번 주 스케쥴 요약")
    public CommonResponse<DashboardWeeklyCalendarResponseDto> showWeekCalendar() {
        //Todo : 비즈니스 로직 작성
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        DashboardWeeklyCalendarResponseDto weeklyCalendarDto = new DashboardWeeklyCalendarResponseDto(weekStart, weekEnd);

        DashboardDailyCalendarResponseDto mon = new DashboardDailyCalendarResponseDto(weekStart);
        mon.addCalendarEvents(List.of(
                new DashboardCalendarEventResponseDto(weekStart.atTime(10, 0), CalendarEventType.INTERVIEW, 2),
                new DashboardCalendarEventResponseDto(weekStart.atTime(9, 30), CalendarEventType.JOB_CLOSE, 1)
        ));

        LocalDate tueDate = weekStart.plusDays(1);
        DashboardDailyCalendarResponseDto tue = new DashboardDailyCalendarResponseDto(tueDate);
        tue.addCalendarEvents(new DashboardCalendarEventResponseDto(tueDate.atTime(9, 30), CalendarEventType.INTERVIEW, 1));

        LocalDate friDate = weekStart.plusDays(4);
        DashboardDailyCalendarResponseDto fri = new DashboardDailyCalendarResponseDto(friDate);
        tue.addCalendarEvents(List.of(
                new DashboardCalendarEventResponseDto(friDate.atTime(11, 20), CalendarEventType.INTERVIEW, 3),
                new DashboardCalendarEventResponseDto(friDate.atTime(21, 40), CalendarEventType.JOB_CLOSE, 1)
        ));

        weeklyCalendarDto.addDailyCalendars(List.of(mon, tue));

        return CommonResponse.success(weeklyCalendarDto);
    }
}
