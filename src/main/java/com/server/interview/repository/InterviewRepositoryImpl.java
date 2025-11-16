package com.server.interview.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.interview.domain.QInterview;
import com.server.jd.domain.QJobDescription;
import com.server.resume.domain.QResume;
import com.server.interview.dto.InterviewSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InterviewRepositoryImpl implements InterviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInterview i = QInterview.interview;
    private final QJobDescription jd = QJobDescription.jobDescription;
    private final QResume r = QResume.resume;

    @Override
    public List<InterviewSummaryDto> searchInterviews(
            Long jdId,
            String status,
            Long cursor,
            String sort,
            int limit
    ) {

        var query = queryFactory
                .select(Projections.constructor(
                        InterviewSummaryDto.class,
                        i.id,                      // interviewId
                        jd.id,                     // jdId
                        jd.title,                  // jdTitle
                        r.name,                    // candidateName
                        i.status.stringValue(),    // InterviewStatus → String
                        i.scheduledAt,
                        i.createdAt
                ))
                .from(i)
                .leftJoin(i.jobDescription, jd)
                .leftJoin(i.resume, r)
                .where(
                        jdId == null ? null : jd.id.eq(jdId),
                        status == null ? null : i.status.stringValue().eq(status),
                        cursor == null ? null : i.id.lt(cursor)
                );

        if ("createdAt,asc".equals(sort)) {
            query.orderBy(i.createdAt.asc());
        } else if ("createdAt,desc".equals(sort)) {
            query.orderBy(i.createdAt.desc());
        } else {
            // 기본 정렬: 최근순
            query.orderBy(i.id.desc());
        }

        // limit + 1로 다음 페이지 판단용
        return query.limit(limit).fetch();
    }
}
