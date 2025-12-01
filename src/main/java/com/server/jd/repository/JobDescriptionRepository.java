package com.server.jd.repository;

import com.server.jd.domain.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long>, JobDescriptionRepositoryCustom {
    List<JobDescription> findAllByStatus(String status);
}
