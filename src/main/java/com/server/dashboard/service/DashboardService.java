package com.server.dashboard.service;

import com.server.dashboard.dto.*;
import com.server.dashboard.repository.DashboardInterviewRepository;
import com.server.dashboard.repository.DashboardJobRepository;
import com.server.dashboard.type.CustomDayOfWeek;
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
    private final DashboardInterviewRepository dashboardRepository;
    private final DashboardJobRepository dashboardJobRepository;

    private List<JobDescription> findActiveJobs() {
        return dashboardJobRepository.findAllByStatus(JobStatus.OPEN);
    }

    private List<Interview> findScheduledInterviews() {
        LocalDateTime startDay = LocalDateTime.now();
        LocalDateTime endDay = startDay.plusDays(7);
        return dashboardRepository.findByScheduledAtBetween(startDay, endDay);
    }

    private <E extends Enum<E>> EnumMap<E, Integer> toEnumCountMap(
            List<? extends CountByStatusInterface> statusList,
            Class<E> enumType
    ) {
        EnumMap<E, Integer> map = new EnumMap<>(enumType);

        for (CountByStatusInterface r : statusList) {
            E status = Enum.valueOf(enumType, r.getStatus());
            map.put(status, r.getCount());
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

        return dashboardRepository.findByScheduledAtBetweenAndResult(oneMonthAgo, now, InterviewResult.PASS).size();
    }

    public List<DashboardApplicantStatsResponseDto> getApplicantStatsForEachJob() {
        return dashboardJobRepository.findJobStatsForEachJobs().stream()
            .map(stats->{
                return new DashboardApplicantStatsResponseDto(
                        stats.getTitle(),
                        new ArrayList<>(List.of(
                                stats.getApplicantCount(),
                                stats.getBookmarkCount(),
                                stats.getInterviewCount(),
                                stats.getPassCount()
                        )),
                        stats.getStatus()
                );
            }).toList();
    }

    public List<DashboardUpcomingInterviewsResponseDto> getUpComingInterviews() {
        return dashboardRepository.findByUpComingInterviews().stream()
            .map((interview)-> DashboardUpcomingInterviewsResponseDto.from(
                    interview.getScheduledTime(),
                    interview.getApplicantName(),
                    interview.getJobTitle(),
                    interview.getInterviewerName()
                )).toList();
    }

    public DashboardJobStatusResponseDto getCountByJobStatus() {
        Map<JobStatus, Integer> map = toEnumCountMap(
                dashboardJobRepository.findCountByJobStatus(),
                JobStatus.class
        );

        return new DashboardJobStatusResponseDto(
                map.get(JobStatus.OPEN),
                map.get(JobStatus.DRAFT),
                map.get(JobStatus.CLOSED)
                );
    }

    public DashboardJobStatusResponseDto getCountByInterviewStatus() {
        Map<InterviewStatus, Integer> map = toEnumCountMap(
                dashboardRepository.findCountByInterviewStatus(),
                InterviewStatus.class
        );

        return new DashboardJobStatusResponseDto(
                map.get(InterviewStatus.IN_PROGRESS),
                map.get(InterviewStatus.WAITING),
                map.get(InterviewStatus.DONE)
        );
    }

    public DashboardWeeklyCalendarResponseDto getWeekCalendarData() {

        LocalDate today = LocalDate.now();
        LocalDate mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sun = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<WeekCalendarInterface> calendarDatas =  dashboardRepository.findWeekCalendarData(mon, sun);

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