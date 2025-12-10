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
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(readOnly = true)
public class DashboardService {
    private final DashboardInterviewRepository dashboardInterviewRepository;
    private final DashboardJobRepository dashboardJobRepository;
    private final UserRepository userRepository;

    private List<JobDescription> findActiveJobs(Long userId) {
        validateUserExists(userId);

        try {
            return dashboardJobRepository.findByAuthor_IdAndStatus(userId, JobStatus.OPEN);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }
    }

    private Long countActiveJobs(Long userId) {
        validateUserExists(userId);

        try {
            return dashboardJobRepository.countByAuthor_IdAndStatus(userId, JobStatus.OPEN);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
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
            } catch (IllegalArgumentException | NullPointerException e) {
                throw ApplicationException.from(DashboardErrorCase.DASHBOARD_INVALID_STATUS_VALUE);
            }
        }

        return map;
    }

    void validateUserExists(Long userId) {
        if (userId == null) {
            throw ApplicationException.from(UserErrorCase.UNAUTHORIZED);
        }
        if (!userRepository.existsById(userId)) {
            throw ApplicationException.from(UserErrorCase.USER_NOT_FOUND);
        }
    }

    public Long getActiveJobsCount(Long userId) {
        return countActiveJobs(userId);
    }

    public long getApplicantsCountOfActiveJobs(Long userId) {
        List<JobDescription> jobDescriptions = findActiveJobs(userId);
        return jobDescriptions.stream()
                .mapToLong(JobDescription::getApplicantCount).sum();
    }

    public Long getScheduledInterviewsCount(Long userId) {
        validateUserExists(userId);

        LocalDateTime startDay = LocalDateTime.now();
        LocalDateTime endDay = startDay.plusDays(7);

        try {
            return dashboardInterviewRepository.countByOrganizer_IdAndScheduledAtBetween(userId, startDay, endDay);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }    }

    public long getMonthHiredCount(Long userId) {
        validateUserExists(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        try {
            return dashboardInterviewRepository.countByOrganizer_IdAndResultAndScheduledAtBetween(userId, InterviewResult.PASS, oneMonthAgo, now);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }
    }

    public List<DashboardApplicantStatsResponseDto> getApplicantStatsForEachJob(Long userId) {
        validateUserExists(userId);

        try {
            return dashboardJobRepository.findJobStatsForEachJobs(userId).stream()
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
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }
    }

    public List<DashboardUpcomingInterviewsResponseDto> getUpComingInterviews(Long userId) {
        validateUserExists(userId);

        try {
            LocalDateTime startDay = LocalDateTime.now();
            LocalDateTime endDay = startDay.plusDays(7);
            return dashboardInterviewRepository.findByUpComingInterviews(userId, startDay, endDay).stream()
                    .map((interview)-> DashboardUpcomingInterviewsResponseDto.from(
                            interview.getScheduledTime(),
                            interview.getApplicantName(),
                            interview.getJobTitle(),
                            interview.getInterviewerName()
                    )).toList();
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }
    }

    public DashboardJobStatusResponseDto getCountByJobStatus(Long userId) {
        validateUserExists(userId);

        List<CountByStatusInterface> list;
        try {
            list = dashboardJobRepository.findCountByJobStatus(userId);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }

        Map<JobStatus, Integer> map = toEnumCountMap(list, JobStatus.class);

        return new DashboardJobStatusResponseDto(
                map.getOrDefault(JobStatus.OPEN, 0),
                map.getOrDefault(JobStatus.DRAFT, 0),
                map.getOrDefault(JobStatus.CLOSED, 0)
        );
    }

    public DashboardJobStatusResponseDto getCountByInterviewStatus(Long userId) {
        validateUserExists(userId);

        List<CountByStatusInterface> list;
        try {
            list = dashboardInterviewRepository.findCountByInterviewStatus(userId);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
        }

        Map<InterviewStatus, Integer> map = toEnumCountMap(list, InterviewStatus.class);

        return new DashboardJobStatusResponseDto(
                map.getOrDefault(InterviewStatus.IN_PROGRESS, 0),
                map.getOrDefault(InterviewStatus.WAITING, 0),
                map.getOrDefault(InterviewStatus.DONE, 0)
        );
    }

    public DashboardWeeklyCalendarResponseDto getWeekCalendarData(Long userId) {
        validateUserExists(userId);

        LocalDate today = LocalDate.now();
        LocalDate mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sun = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<WeekCalendarInterface> calendarDatas;
        try {
            calendarDatas =  dashboardInterviewRepository.findWeekCalendarData(userId, mon, sun);
        } catch (Exception e) {
            throw ApplicationException.from(DashboardErrorCase.DASHBOARD_QUERY_FAIL);
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