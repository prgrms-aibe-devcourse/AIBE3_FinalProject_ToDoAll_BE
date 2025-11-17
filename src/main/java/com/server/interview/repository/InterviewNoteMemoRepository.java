package com.server.interview.repository;

import com.server.interview.domain.InterviewNoteMemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewNoteMemoRepository extends JpaRepository<InterviewNoteMemo, Long> {
}
