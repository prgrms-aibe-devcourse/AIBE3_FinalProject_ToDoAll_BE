package com.server.dashboard.repository;

import com.server.dashboard.dto.CountByStatusInterface;
import com.server.dashboard.dto.JobStatsInterface;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardJobRepository extends JpaRepository<JobDescription,Long> {
    List<JobDescription> findByAuthor_IdAndStatus(Long id, JobStatus status);

    Long countByAuthor_IdAndStatus(Long userId, JobStatus jobStatus);

    @Query(value = """
        select label.status, count(jd.id) as count
        from (
                 SELECT 'DRAFT' AS status
                 UNION ALL SELECT 'OPEN'
                 UNION ALL SELECT 'CLOSED'
             ) as label
                 left join job_descriptions jd
                           on label.status = jd.status
                               and jd.author_id = :userId
        group by label.status;
    """, nativeQuery = true)
    List<CountByStatusInterface> findCountByJobStatus(Long userId);

    @Query(value= """
        select
          result.title,
          if(result.interview_count=0, 'DOCUMENT',
             if(result.pending_count>0, 'INTERVIEW', 'FINISHED')) as status,
          result.applicant_count,
          result.bookmark_count,
          result.interview_count,
          result.pass_count
        from (
          select
            jd.id,
            jd.title,
            jd.deadline,
            count(distinct res.id) as applicant_count,
            count(distinct if(res.status='BOOKMARK', res.id, null)) as bookmark_count,
            count(distinct iv.id) as interview_count,
            count(distinct if(iv.result='PASS', iv.id, null)) as pass_count,
            count(distinct if(iv.result='PENDING', iv.id, null)) as pending_count
          from job_descriptions jd
          left join resumes res on jd.id = res.jd_id
          left join interview iv on jd.id = iv.jd_id and res.id = iv.resume_id
          where jd.author_id = :userId and jd.status != 'DRAFT'
          group by jd.id, jd.title, jd.deadline
        ) as result
        order by result.deadline desc
        limit 3;
    """, nativeQuery = true)
    List<JobStatsInterface> findJobStatsForEachJobs(@Param("userId") Long userId);
}
