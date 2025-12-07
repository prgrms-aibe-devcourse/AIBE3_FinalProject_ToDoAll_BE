package com.server.dashboard.repository;

import com.server.dashboard.dto.CountByStatus;
import com.server.dashboard.dto.UpComingInterviewInterface;
import com.server.dashboard.dto.WeekCalendarInterface;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewResult;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardInterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByScheduledAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Interview> findByScheduledAtBetweenAndResult(LocalDateTime startDate, LocalDateTime endDate, InterviewResult result);

    @Query(value= """
        select
            iv.id as interview_id,
            iv.scheduled_at as scheduled_time,
            jd.title as job_title,
            rs.name as applicant_name,
            coalesce(group_concat(users.name separator ', '), '미정') as interviewer_name
        from interview iv
        left join job_descriptions jd on iv.jd_id = jd.id
        left join resumes rs on iv.resume_id = rs.id
        left join interview_participant ip on iv.id = ip.interview_id
        left join users on ip.user_id = users.id
        where iv.status = 'WAITING'
        group by iv.id, rs.id
    """, nativeQuery = true)
    List<UpComingInterviewInterface> findByUpComingInterviews();

    @Query(value = """
        select label.status, count(iv.id) as count from
            (
                select 'WAITING' as status
                union all select 'IN_PROGRESS'
                union  all select 'DONE'
            ) as label
        left join interview iv on label.status = iv.status
        group by label.status
    """, nativeQuery = true)
    List<CountByStatus> findCountByInterviewStatus();

    @Query(value= """
        select id, title, time, type from
        (
            select iv.id as id, rs.name as title, iv.scheduled_at as time, 'INTERVIEW' as type from interview iv
            left join resumes rs on iv.resume_id = rs.id
            where iv.scheduled_at >= :startDate and
                  iv.scheduled_at <= :endDate
        ) calendar
        union all
        (
            select id, title, deadline as time, 'JOB_CLOSE' as type from job_descriptions
            where deadline >= :startDate and
                  deadline <= :endDate
        )
        order by time
    """, nativeQuery = true)
    List<WeekCalendarInterface> findWeekCalendarData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
