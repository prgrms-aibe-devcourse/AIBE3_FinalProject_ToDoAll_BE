package com.server.dashboard.service;

import com.server.dashboard.dto.*;
import com.server.dashboard.repository.DashboardInterviewRepository;
import com.server.dashboard.repository.DashboardJobRepository;
import com.server.dashboard.type.CalendarEventType;
import com.server.dashboard.type.JobStatusOfProgress;
import com.server.interview.domain.InterviewResult;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.user.domain.Gender;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {
    @Mock
    private DashboardJobRepository dashboardJobRepository;

    @Mock
    private DashboardInterviewRepository dashboardInterviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private List<JobDescription> testJobs;

    private final Long userId = 1L;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        User testAuthor = User.of(
                "test@test.com",
                "encodedPassword",
                "Author",
                "AuthorNick",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "TestCompany",
                "Developer"
        );

        testJobs = new ArrayList<>();
        testJobs.addAll(List.of(JobDescription.of(
                        "테스트 JD 1", "개발팀", "정규직", "주니어", "학사", "5000",
                        "상세 설명", null, LocalDate.now(KST).plusDays(30),
                        JobStatus.OPEN, "복지", 5L, "서울", "thumbnail.url", testAuthor
                ),
                JobDescription.of(
                        "테스트 JD 2", "운영팀", "이사", "주니어", "석사", "5000",
                        "상세 설명", null, LocalDate.now(KST).plusDays(30),
                        JobStatus.OPEN, "복지", 1L, "서울", "thumbnail.url", testAuthor
                ),
                JobDescription.of(
                        "테스트 JD 3", "출장팀", "대표", "주니어", "학사", "5000",
                        "상세 설명", null, LocalDate.now(KST).plusDays(30),
                        JobStatus.OPEN, "복지", 7L, "서울", "thumbnail.url", testAuthor
                )));

        given(userRepository.existsById(anyLong())).willReturn(true);
    }

    @Test
    public void getActiveJobsCount() {
        // given
        Long result = 1L;
        given(dashboardJobRepository.countByAuthor_IdAndStatus(anyLong(), any(JobStatus.class)))
                .willReturn(result);

        // When
        Long count = dashboardService.getActiveJobsCount(userId);

        // then
        assertEquals(result, count);
    }

    @Test
    public void getApplicantsCountOfActiveJobs() {
        // given
        Long result = this.testJobs.stream().mapToLong(JobDescription::getApplicantCount).sum();

        given(dashboardJobRepository.findByAuthor_IdAndStatus(anyLong(), any(JobStatus.class)))
                .willReturn(testJobs);

        // When
        Long countApplicant = dashboardService.getApplicantsCountOfActiveJobs(userId);

        // then
        assertEquals(result, countApplicant);
    }

    @Test
    public void getScheduledInterviewsCount() {
        // given
        Long result = 4L;
        given(dashboardInterviewRepository.countByOrganizer_IdAndScheduledAtBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(result);

        // When
        Long countInterview = dashboardService.getScheduledInterviewsCount(userId);

        // then
        assertEquals(result, countInterview);
    }

    @Test
    public void getMonthHiredCount() {
        // given
        Long result = 4L;
        given(dashboardInterviewRepository.countByOrganizer_IdAndResultAndScheduledAtBetween(
                anyLong(),
                any(InterviewResult.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .willReturn(result);

        // When
        Long countHires = dashboardService.getMonthHiredCount(userId);

        // then
        assertEquals(result, countHires);
    }

    @Test
    public void getApplicantStatsForEachJob() {
        // given
        List<JobStatsInterface> jis =  new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)).stream().map(
                num-> {
                    JobStatsInterface js = mock(JobStatsInterface.class);
                    given(js.getTitle()).willReturn("title"+num);
                    given(js.getStatus()).willReturn(JobStatusOfProgress.DOCUMENT);
                    given(js.getApplicantCount()).willReturn(num+1);
                    given(js.getBookmarkCount()).willReturn(num+2);
                    given(js.getInterviewCount()).willReturn(num+3);
                    given(js.getPassCount()).willReturn(num+4);
                    return js;
                }
        ).toList();

        given(dashboardJobRepository.findJobStatsForEachJobs(anyLong()))
                .willReturn(jis);

        // When
        List<DashboardApplicantStatsResponseDto> responseDtos = dashboardService.getApplicantStatsForEachJob(userId);

        // then
        assertEquals(responseDtos.size(), jis.size());
        assertEquals(jis.getFirst().getTitle(), responseDtos.getFirst().title());
        assertEquals(jis.getLast().getTitle(), responseDtos.getLast().title());
    }

    @Test
    public void getUpComingInterviews() {
        // given
        List<UpComingInterviewInterface> uiis =  new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)).stream().map(
                num-> {
                    UpComingInterviewInterface uii = mock(UpComingInterviewInterface.class);
                    given(uii.getScheduledTime()).willReturn(now(KST));
                    given(uii.getJobTitle()).willReturn("Jobtitle"+num);
                    given(uii.getApplicantName()).willReturn("applicantName"+num);
                    given(uii.getInterviewerName()).willReturn("interviewerName"+num);
                    return uii;
                }
        ).collect(Collectors.toCollection(ArrayList::new));
        given(dashboardInterviewRepository.findByUpComingInterviews(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(uiis);

        // when
        List<DashboardUpcomingInterviewsResponseDto> responseDtos = dashboardService.getUpComingInterviews(userId);

        // then
        assertEquals(uiis.size(), responseDtos.size());
        assertEquals(uiis.getFirst().getJobTitle(), responseDtos.getFirst().jobTitle());
        assertEquals(uiis.getLast().getJobTitle(), responseDtos.getLast().jobTitle());
    }

    @Test
    public void getCountByJobStatus() {
        // given
        List<CountByStatusInterface> list =  new ArrayList<>(Arrays.asList(1, 2, 3)).stream().map(
                num-> {
                    CountByStatusInterface iv = mock(CountByStatusInterface.class);
                    if (num==1) given(iv.getStatus()).willReturn("OPEN");
                    else if (num==2) given(iv.getStatus()).willReturn("DRAFT");
                    else given(iv.getStatus()).willReturn("CLOSED");
                    given(iv.getCount()).willReturn(num);
                    return iv;
                }
        ).toList();

        given(dashboardJobRepository.findCountByJobStatus(anyLong()))
                .willReturn(list);

        // when
        DashboardJobStatusResponseDto responseDto = dashboardService.getCountByJobStatus(userId);

        // then
        assertEquals(1, responseDto.in());
        assertEquals(2, responseDto.before());
        assertEquals(3, responseDto.close());
    }

    @Test
    public void getCountByInterviewStatus() {
        // given
        List<CountByStatusInterface> list =  new ArrayList<>(Arrays.asList(1, 2, 3)).stream().map(
                num-> {
                    CountByStatusInterface iv = mock(CountByStatusInterface.class);
                    if (num==1) given(iv.getStatus()).willReturn("IN_PROGRESS");
                    else if (num==2) given(iv.getStatus()).willReturn("WAITING");
                    else given(iv.getStatus()).willReturn("DONE");
                    given(iv.getCount()).willReturn(num);
                    return iv;
                }
        ).toList();

        given(dashboardInterviewRepository.findCountByInterviewStatus(anyLong()))
                .willReturn(list);

        // when
        DashboardJobStatusResponseDto responseDto = dashboardService.getCountByInterviewStatus(userId);

        // then
        assertEquals(1, responseDto.in());
        assertEquals(2, responseDto.before());
        assertEquals(3, responseDto.close());
    }

    @Test
    public void getWeekCalendarData() {
        LocalDate today = LocalDate.now(KST);
        LocalDate mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sun = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // given
        List<WeekCalendarInterface> wcis =  new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)).stream().map(
                num-> {
                    WeekCalendarInterface uii = mock(WeekCalendarInterface.class);
                    given(uii.getId()).willReturn((long) num);
                    given(uii.getTitle()).willReturn("Jobtitle"+num);
                    if (num<3) given(uii.getTime()).willReturn(mon.plusDays(3).atStartOfDay());
                    else given(uii.getTime()).willReturn(mon.plusDays(4).atStartOfDay());
                    given(uii.getType()).willReturn(CalendarEventType.INTERVIEW);
                    return uii;
                }
        ).collect(Collectors.toCollection(ArrayList::new));
        given(dashboardInterviewRepository.findWeekCalendarData(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(wcis);

        // when
        DashboardWeeklyCalendarResponseDto responseDto = dashboardService.getWeekCalendarData(userId);

        // then
        assertEquals(mon, responseDto.weekStart());
        assertEquals(sun, responseDto.weekEnd());
        assertEquals(0, responseDto.dailyCalendars().mon().events().size());
        assertEquals(0, responseDto.dailyCalendars().tue().events().size());
        assertEquals(0, responseDto.dailyCalendars().wed().events().size());
        assertEquals(2, responseDto.dailyCalendars().thu().events().size());
        assertEquals(3, responseDto.dailyCalendars().fri().events().size());
        assertEquals(0, responseDto.dailyCalendars().sat().events().size());
        assertEquals(0, responseDto.dailyCalendars().sun().events().size());
    }
}
