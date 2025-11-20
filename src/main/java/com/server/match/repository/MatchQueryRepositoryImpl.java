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
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class MatchQueryRepositoryImpl implements MatchQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MatchListResponseDto> searchMatches(MatchSearchCondition condition, Pageable pageable) {
        QMatch match = QMatch.match;
        QResume resume = QResume.resume;

        JPAQuery<MatchListResponseDto> baseQuery = queryFactory
                .select(Projections.constructor(
                        MatchListResponseDto.class,
                        resume.id,
                        resume.name,
                        match.matchScore,
                        match.status,
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

        if (condition.getSortSafe() == MatchSortType.SCORE_DESC) {
            baseQuery.orderBy(match.matchScore.desc());
        } else {
            baseQuery.orderBy(match.appliedAt.desc());
        }

        List<MatchListResponseDto> content = baseQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = baseQuery.fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}
