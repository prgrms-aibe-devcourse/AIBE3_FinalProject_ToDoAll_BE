package com.server.dashboard.controller;

import com.server.dashboard.dto.DashboardApplicantStatsResponseDto;
import com.server.dashboard.dto.DashboardJobStatusResponseDto;
import com.server.dashboard.dto.DashboardUpcomingInterviewsResponseDto;
import com.server.dashboard.dto.DashboardWeeklyCalendarResponseDto;
import com.server.dashboard.service.DashboardService;
import com.server.global.config.security.jwt.JwtAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("요약카드 - 활성 공고 수 조회 API")
    void showActiveJobs() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        Long activeJobsCount = 5L;
        given(dashboardService.getActiveJobsCount(anyLong()))
                .willReturn(activeJobsCount);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/summary/actives")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(activeJobsCount));
    }

    @Test
    @DisplayName("요약카드 - 총 지원자 수 조회 API")
    void showAllApplicants() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        Long applicantsCount = 23L;
        given(dashboardService.getApplicantsCountOfActiveJobs(anyLong()))
                .willReturn(applicantsCount);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/summary/applicants")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(applicantsCount));
    }

    @Test
    @DisplayName("요약카드 - 예정된 면접 수 조회 API")
    void showScheduledInterviews() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        Long interviewCount = 3L;
        given(dashboardService.getScheduledInterviewsCount(anyLong()))
                .willReturn(interviewCount);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/summary/interviews")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(interviewCount));
    }

    @Test
    @DisplayName("요약카드 - 이번 달 합격자 수 조회 API")
    void showHired() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        Long hiredCount = 7L;
        given(dashboardService.getMonthHiredCount(anyLong()))
                .willReturn(hiredCount);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/summary/hires")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(hiredCount));
    }

    @Test
    @DisplayName("상세 - 공고별 지원자 현황 조회 API")
    void showJobResult() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        List<DashboardApplicantStatsResponseDto> stats =
                List.of(mock(DashboardApplicantStatsResponseDto.class));

        given(dashboardService.getApplicantStatsForEachJob(anyLong()))
                .willReturn(stats);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/detail/applicant-stat-byJob")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("상세 - 다가오는 면접 일정 조회 API")
    void showUpcomingInterviews() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        List<DashboardUpcomingInterviewsResponseDto> upcoming =
                List.of(mock(DashboardUpcomingInterviewsResponseDto.class));

        given(dashboardService.getUpComingInterviews(anyLong()))
                .willReturn(upcoming);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/detail/upcoming-interview")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("상세 - 공고 상태별 현황 조회 API")
    void showJobStatus() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        DashboardJobStatusResponseDto dto = mock(DashboardJobStatusResponseDto.class);
        given(dashboardService.getCountByJobStatus(anyLong()))
                .willReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/detail/job-status")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("상세 - 면접 상태별 현황 조회 API")
    void showInterviewStatus() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        DashboardJobStatusResponseDto dto = mock(DashboardJobStatusResponseDto.class);
        given(dashboardService.getCountByInterviewStatus(anyLong()))
                .willReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/detail/interview-status")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("이번 주 캘린더 조회 API")
    void showWeekCalendar() throws Exception {
        // given
        Long userId = 1L;
        Authentication auth = new JwtAuthentication(userId);

        DashboardWeeklyCalendarResponseDto dto = mock(DashboardWeeklyCalendarResponseDto.class);
        given(dashboardService.getWeekCalendarData(anyLong()))
                .willReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/week-calendar")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap());
    }
}
