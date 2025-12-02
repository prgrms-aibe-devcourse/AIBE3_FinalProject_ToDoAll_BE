package com.server.search.repository;

import com.server.search.domain.ResumeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeSummaryRepository extends JpaRepository<ResumeSummary, Long> {
}
