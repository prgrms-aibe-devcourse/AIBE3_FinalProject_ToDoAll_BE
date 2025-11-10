package com.server.jd.repository;

import com.server.jd.domain.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findAllByStatus(String status);

    @Query("""
            select jd.id as jobId, s
            from JobDescription jd
            join jd.requiredSkills s
            where jd.id in :ids
            """)
    List<Object[]> findRequiredSkillsByJobIds(Collection<Long> ids);
}
