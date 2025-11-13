package com.server.match.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.match.domain.MatchSortType;
import com.server.match.domain.QMatch;
import com.server.match.dto.MatchListResponseDto;
import com.server.match.dto.MatchSearchCondition;
import com.server.resume.domain.QResume;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.querydsl.core.types.dsl.Expressions;
import java.util.List;

@RequiredArgsConstructor
public class MatchQueryRepositoryImpl implements MatchQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MatchListResponseDto> searchMatches(MatchSearchCondition condition) {
        QMatch match = QMatch.match;
        QResume resume = QResume.resume;

        JPAQuery<MatchListResponseDto> baseQuery = queryFactory
                .select(Projections.constructor(
                        MatchListResponseDto.class,
                        resume.id,
                        resume.name,
                        match.matchScore,
                        match.status,
                        // ES 도입 전 임시 데이터
                        Expressions.constant("75%"),
                        Expressions.constant(List.of("Kafka", "Redis")),
                        Expressions.constant("React/Node.js 기반 3년 경력 보유")
                ))
                .from(match)
                .join(match.resume, resume)
                .where(match.jobDescription.id.eq(condition.jdId()));

        if (condition.status() != null) {
            baseQuery.where(match.status.eq(condition.status()));
        }

        // 정렬 기준
        if (condition.sort() == MatchSortType.SCORE_DESC) {
            baseQuery.orderBy(match.matchScore.desc());
        } else {
            baseQuery.orderBy(match.createdAt.desc());
        }

        int page = condition.getPage();
        int size = condition.getPageSize();

        List<MatchListResponseDto> content = baseQuery
                .offset((long) page * size)
                .limit(size)
                .fetch();

        return new PageImpl<>(content, PageRequest.of(page, size), content.size());
    }
}
