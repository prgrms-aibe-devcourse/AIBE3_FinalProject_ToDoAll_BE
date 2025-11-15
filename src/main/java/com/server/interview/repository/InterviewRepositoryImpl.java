package com.server.interview.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewStatus;
import com.server.interview.domain.QInterview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InterviewRepositoryImpl implements InterviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInterview i = QInterview.interview;

    @Override
    public List<Interview> searchInterviews(
            Long jdId,
            String status,
            Long cursor,
            String sort,
            int limit
    ) {

        var query = queryFactory
                .selectFrom(i)
                .where(
                        jdId == null ? null : i.jobDescription.id.eq(jdId),
                        status == null ? null : i.status.eq(InterviewStatus.valueOf(status)),
                        cursor == null ? null : i.id.lt(cursor)
                );

        // 정렬 분기 (CASE WHEN 제거 → 인덱스 기반 정렬 가능)
        if ("createdAt,asc".equals(sort)) {
            query.orderBy(i.createdAt.asc());
        } else if ("createdAt,desc".equals(sort)) {
            query.orderBy(i.createdAt.desc());
        } else {
            // 기본 정렬
            query.orderBy(i.id.desc());
        }

        return query
                .limit(limit)
                .fetch();
    }
}
