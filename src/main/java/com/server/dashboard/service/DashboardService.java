package com.server.dashboard.service;

import com.server.dashboard.dto.*;
import com.server.dashboard.exception.DashboardErrorCase;
import com.server.dashboard.repository.DashboardInterviewRepository;
import com.server.dashboard.repository.DashboardJobRepository;
import com.server.dashboard.type.CustomDayOfWeek;
import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewResult;
import com.server.interview.domain.InterviewStatus;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.server.dashboard.util.Formatter.formatterTimeWithAMPM;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardInterviewRepository dashboardInterviewRepository;
    private final DashboardJobRepository dashboardJobRepository;

    private List<JobDescription> findActiveJobs() {
        try {
            return dashboardJobRepository.findAllByStatus(JobStatus.OPEN);
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    private List<Interview> findScheduledInterviews() {
        LocalDateTime startDay = LocalDateTime.now();
        LocalDateTime endDay = startDay.plusDays(7);

        try {
            return dashboardInterviewRepository.findByScheduledAtBetween(startDay, endDay);
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    private <E extends Enum<E>> EnumMap<E, Integer> toEnumCountMap(
            List<? extends CountByStatusInterface> statusList,
            Class<E> enumType
    ) {
        EnumMap<E, Integer> map = new EnumMap<>(enumType);

        for (CountByStatusInterface r : statusList) {
            try {
                E status = Enum.valueOf(enumType, r.getStatus());
                map.put(status, r.getCount());
            } catch (IllegalArgumentException e) {
                throw new ApplicationException(DashboardErrorCase.DASHBOARD_INVALID_STATUS_VALUE, e);
            }
        }

        return map;
    }

    public int getActiveJobsCount() {
        return findActiveJobs().size();
    }

    public long getApplicantsCountOfActiveJobs() {
        List<JobDescription> jobDescriptions = findActiveJobs();
        return jobDescriptions.stream()
                .mapToLong(JobDescription::getApplicantCount).sum();
    }

    public int getScheduledInterviewsCount() {
        return findScheduledInterviews().size();
    }

    public long getMonthHiredCount() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        try {
            return dashboardInterviewRepository.findByScheduledAtBetweenAndResult(oneMonthAgo, now, InterviewResult.PASS).size();
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    public List<DashboardApplicantStatsResponseDto> getApplicantStatsForEachJob() {
        try {
            return dashboardJobRepository.findJobStatsForEachJobs().stream()
                    .map(stats-> new DashboardApplicantStatsResponseDto(
                            stats.getTitle(),
                            new ArrayList<>(List.of(
                                    stats.getApplicantCount(),
                                    stats.getBookmarkCount(),
                                    stats.getInterviewCount(),
                                    stats.getPassCount()
                            )),
                            stats.getStatus()
                    )).toList();
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    public List<DashboardUpcomingInterviewsResponseDto> getUpComingInterviews() {
        try {
            return dashboardInterviewRepository.findByUpComingInterviews().stream()
                    .map((interview)-> DashboardUpcomingInterviewsResponseDto.from(
                            interview.getScheduledTime(),
                            interview.getApplicantName(),
                            interview.getJobTitle(),
                            interview.getInterviewerName()
                    )).toList();
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    public DashboardJobStatusResponseDto getCountByJobStatus() {
        try {
            Map<JobStatus, Integer> map = toEnumCountMap(
                    dashboardJobRepository.findCountByJobStatus(),
                    JobStatus.class
            );

            return new DashboardJobStatusResponseDto(
                    map.getOrDefault(JobStatus.OPEN, 0),
                    map.getOrDefault(JobStatus.DRAFT, 0),
                    map.getOrDefault(JobStatus.CLOSED, 0)
            );
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    public DashboardJobStatusResponseDto getCountByInterviewStatus() {
        try {
            Map<InterviewStatus, Integer> map = toEnumCountMap(
                    dashboardInterviewRepository.findCountByInterviewStatus(),
                    InterviewStatus.class
            );

            return new DashboardJobStatusResponseDto(
                    map.getOrDefault(InterviewStatus.IN_PROGRESS, 0),
                    map.getOrDefault(InterviewStatus.WAITING, 0),
                    map.getOrDefault(InterviewStatus.DONE, 0)
            );
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }
    }

    public DashboardWeeklyCalendarResponseDto getWeekCalendarData() {

        LocalDate today = LocalDate.now();
        LocalDate mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sun = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<WeekCalendarInterface> calendarDatas;
        try {
            calendarDatas =  dashboardInterviewRepository.findWeekCalendarData(mon, sun);
        } catch (Exception e) {
            throw new ApplicationException(DashboardErrorCase.DASHBOARD_QUERY_FAIL, e);
        }

        DashboardWeeklyCalendarResponseDto weeklyCalendarDto = new DashboardWeeklyCalendarResponseDto(mon, sun);

        for (WeekCalendarInterface data : calendarDatas) {
            CustomDayOfWeek day = CustomDayOfWeek.from(data.getTime().getDayOfWeek());
            weeklyCalendarDto.addCalendarEvent(day,
                    new DashboardCalendarEventDto(
                        data.getId(),
                        data.getTime().format(formatterTimeWithAMPM),
                        data.getTitle(),
                        data.getType()
                    )
            );
        }

        return weeklyCalendarDto;
    }
}