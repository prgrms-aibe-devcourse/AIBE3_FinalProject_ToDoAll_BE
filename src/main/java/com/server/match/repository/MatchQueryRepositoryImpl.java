package com.server.match.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.ai.service.AiRecommendationService;
import com.server.ai.service.KeywordExtractorService;
import com.server.jd.domain.JobDescription;
import com.server.match.domain.Match;
import com.server.match.domain.QMatch;
import com.server.match.dto.MatchListResponseDto;
import com.server.match.dto.MatchSearchCondition;
import com.server.match.util.MatchScoreCalculator;
import com.server.resume.domain.QResume;
import com.server.resume.domain.Resume;
import com.server.s3.service.PresignedUrlProvider;
import com.server.search.document.ResumeDocument;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RequiredArgsConstructor
public class MatchQueryRepositoryImpl implements MatchQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ResumeSearchService resumeSearchService;
    private final KeywordExtractorService keywordExtractorService;
    private final AiRecommendationService aiRecommendationService;
    private final PresignedUrlProvider presignedUrlProvider;

    @Override
    public Page<MatchListResponseDto> searchMatches(MatchSearchCondition condition, Pageable pageable) {

        QMatch match = QMatch.match;
        QResume resume = QResume.resume;

        JPAQuery<Match> query = queryFactory
                .selectFrom(match)
                .join(match.resume, resume).fetchJoin()
                .where(match.jobDescription.id.eq(condition.jdId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (condition.status() != null) {
            query.where(match.status.eq(condition.status()));
        }

        // 정렬 적용
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                query.orderBy(toOrderSpecifier(order.getProperty(), order.getDirection(), match));
            }
        } else {
            query.orderBy(match.appliedAt.desc());
        }

        List<Match> matches = query.fetch();
        long total = query.fetchCount();

        JobDescription jd = matches.isEmpty() ? null : matches.get(0).getJobDescription();
        List<String> jdKeywords = jd != null
                ? keywordExtractorService.extractKeywords(jd.getDescription())
                : List.of();

        // DTO 매핑
        List<MatchListResponseDto> content = matches.stream().map(m -> {
            Resume resumeEntity = m.getResume();
            ResumeDocument doc = resumeSearchService.find(resumeEntity.getId())
                    .orElseGet(() -> ResumeDocument.of(resumeEntity));

            List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

            int totalSkills = missingSkills.size() + doc.getSkills().size();
            String matchRate = totalSkills > 0
                    ? Math.round(((float) (totalSkills - missingSkills.size()) / totalSkills) * 100) + "%"
                    : "0%";

            String summary = m.getResumeSummary();
            if (summary == null || summary.isBlank()) {
                summary = aiRecommendationService.generateResumeSummary(doc.getFullText());
            }

            String profileImageUrl = null;
            if (resumeEntity.getResumeFileUrl() != null) {
                profileImageUrl =
                        presignedUrlProvider.createPresignedGetUrl(
                                resumeEntity.getResumeFileUrl()
                        );
            }

            return new MatchListResponseDto(
                    resumeEntity.getId(),
                    resumeEntity.getName(),
                    profileImageUrl,
                    m.getMatchScore(),
                    m.getStatus(),
                    matchRate,
                    missingSkills,
                    doc.getSkills(),
                    summary
            );
        }).toList();

        return new PageImpl<>(content, pageable, total);
    }

    /** 정렬 필드 매핑 */
    private OrderSpecifier<?> toOrderSpecifier(String property, Sort.Direction direction, QMatch match) {

        // createdAt → appliedAt 매핑
        String mapped = property.equals("createdAt") ? "appliedAt" : property;

        return switch (mapped) {
            case "appliedAt" ->
                    direction.isAscending() ? match.appliedAt.asc() : match.appliedAt.desc();
            case "matchScore" ->
                    direction.isAscending() ? match.matchScore.asc() : match.matchScore.desc();
            default ->
                    match.appliedAt.desc();
        };
    }
}
