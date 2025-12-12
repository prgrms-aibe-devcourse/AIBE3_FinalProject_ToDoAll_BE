package com.server.dashboard.repository;

import com.server.dashboard.dto.CountByStatusInterface;
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
    Long countByOrganizer_IdAndScheduledAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Long countByOrganizer_IdAndResultAndScheduledAtBetween(Long userId, InterviewResult result, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value= """
        select
            if(iv.organizer_id = :userId, 'true', 'false') as is_organizer,
            iv.id as interview_id,
            rs.id as resume_id,
            iv.scheduled_at as scheduled_time,
            jd.title as job_title,
            rs.name as applicant_name,
            coalesce(group_concat(users.name separator ', '), '미정') as interviewer_name
        from interview iv
                 left join job_descriptions jd on iv.jd_id = jd.id
                 left join resumes rs on iv.resume_id = rs.id
                 left join interview_participant ip on iv.id = ip.interview_id
                 left join users on ip.user_id = users.id
        where :userId in (
            select user_id from interview_participant sub_ip
            where sub_ip.interview_id=iv.id
        )
            and (iv.status = 'WAITING' or iv.status = 'IN_PROGRESS')
            and iv.scheduled_at >= :startDate
            and iv.scheduled_at < :endDate
        group by iv.id, rs.id;
    """, nativeQuery = true)
    List<UpComingInterviewInterface> findByUpComingInterviews(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")LocalDateTime endDate
    );

    @Query(value = """
        select label.status, count(iv.id) as count from
            (
                select 'WAITING' as status
                union all select 'IN_PROGRESS'
                union  all select 'DONE'
            ) as label
                left join interview iv
                    on label.status = iv.status
                    and iv.organizer_id = :userId
        group by label.status
    """, nativeQuery = true)
    List<CountByStatusInterface> findCountByInterviewStatus(@Param("userId") Long userId);

    @Query(value= """
        select id, title, time, type
        from (
            select iv.id as id, rs.name as title, iv.scheduled_at as time, 'INTERVIEW' as type
            from interview iv
            left join resumes rs on iv.resume_id = rs.id
            where :userId in (
                select user_id from interview_participant ip
                where ip.interview_id=iv.id
            )
              and iv.scheduled_at >= :startDate
              and iv.scheduled_at < :endDate
    
            union all
    
            select jd.id as id, jd.title as title, jd.deadline as time, 'JOB_CLOSE' as type
            from job_descriptions jd
            where jd.author_id = :userId
              and jd.deadline >= :startDate
              and jd.deadline < :endDate
        ) calendar
        order by time
    """, nativeQuery = true)
    List<WeekCalendarInterface> findWeekCalendarData(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
