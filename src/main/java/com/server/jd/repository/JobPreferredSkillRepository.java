package com.server.jd.repository;

import com.server.jd.domain.JobPreferredSkill;
import com.server.jd.repository.projection.SkillByJobProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface JobPreferredSkillRepository extends JpaRepository<JobPreferredSkill, Long> {
    @Query("""
        select jps.job.id as jobId, jps.skill.name
        from JobPreferredSkill jps
        where jps.job.id in :ids
    """)
    List<SkillByJobProjection> findPreferredSkillsByJobIds(Collection<Long> ids);

    @Query("""
        select jps.skill.name
        from JobPreferredSkill jps
        where jps.job.id = :id
    """)
    List<String> findPreferredSkillNamesByJobId(Long id);
}