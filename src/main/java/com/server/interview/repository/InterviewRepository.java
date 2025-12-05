package com.server.interview.repository;

import com.server.interview.domain.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long>, InterviewRepositoryCustom {

    @Modifying
    @Query("""
        UPDATE Interview i
        SET i.status = 'IN_PROGRESS'
        WHERE i.status = 'WAITING'
          AND i.scheduledAt <= :now
    """)
    void updateInProgress(LocalDateTime now);

    @Query("select count(i) from Interview i where i.scheduledAt >= :today")
    long countToday(LocalDateTime today);

    @Query("SELECT i.id FROM Interview i WHERE i.scheduledAt < :thresholdDate")
    List<Long> findInterviewIdsOlderThan(LocalDateTime thresholdDate);
}
