package com.server.interview.repository;

import com.server.interview.domain.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query(value = """
        SELECT *
        FROM interview i
        WHERE (:jdId IS NULL OR i.jd_id = :jdId)
          AND (:status IS NULL OR i.status = :status)
          AND (:cursor IS NULL OR i.id < :cursor)
        ORDER BY
            CASE WHEN :sort = 'createdAt,asc'  THEN i.created_at END ASC,
            CASE WHEN :sort = 'createdAt,desc' THEN i.created_at END DESC
        LIMIT :limit
        """,
            nativeQuery = true
    )
    List<Interview> searchInterviews(
            @Param("jdId") Long jdId,
            @Param("status") String status,  // Enum 그대로 사용
            @Param("cursor") Long cursor,
            @Param("sort") String sort,
            @Param("limit") int limit
    );
}
