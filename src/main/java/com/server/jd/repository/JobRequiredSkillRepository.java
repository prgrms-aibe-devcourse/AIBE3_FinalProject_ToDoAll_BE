package com.server.jd.repository;

import com.server.jd.domain.JobRequiredSkill;
import com.server.jd.repository.projection.SkillByJobProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;



public interface JobRequiredSkillRepository extends JpaRepository<JobRequiredSkill, Long> {
    @Query("""
        select jrs.job.id as jobId, jrs.skill.name as skillName
        from JobRequiredSkill jrs
        where jrs.job.id in :ids
    """)
    List<SkillByJobProjection> findRequiredSkillsByJobIds(Collection<Long> ids);

    @Query("""
        select jrs.skill.name
        from JobRequiredSkill jrs
        where jrs.job.id = :id
    """)
    List<String> findRequiredSkillNamesByJobId(Long id);
}
