package com.server.match.repository;

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
import com.server.search.document.ResumeDocument;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class MatchQueryRepositoryImpl implements MatchQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ResumeSearchService resumeSearchService;
    private final KeywordExtractorService keywordExtractorService;
    private final AiRecommendationService aiRecommendationService;

    @Override
    public Page<MatchListResponseDto> searchMatches(MatchSearchCondition condition, Pageable pageable) {
        QMatch match = QMatch.match;
        QResume resume = QResume.resume;

        // 1차 쿼리로 Match + Resume fetch
        JPAQuery<Match> query = queryFactory
                .selectFrom(match)
                .join(match.resume, resume).fetchJoin()
                .where(match.jobDescription.id.eq(condition.jdId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (condition.status() != null) {
            query.where(match.status.eq(condition.status()));
        }

        query.orderBy(match.appliedAt.desc());  // 지원 최신순 정렬

        List<Match> matches = query.fetch();
        long total = query.fetchCount();

        JobDescription jd = matches.isEmpty() ? null : matches.get(0).getJobDescription();
        List<String> jdKeywords = jd != null
                ? keywordExtractorService.extractKeywords(jd.getDescription())
                : List.of();

        // DTO 수동 매핑
        List<MatchListResponseDto> content = matches.stream().map(m -> {
            Resume resumeEntity = m.getResume();
            ResumeDocument doc = resumeSearchService.find(resumeEntity.getId())
                    .orElseGet(() -> ResumeDocument.of(resumeEntity));

            // 누락된 기술
            List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

            // 기술 매칭률
            int totalSkills = missingSkills.size() + doc.getSkills().size();
            String matchRate = totalSkills > 0
                    ? Math.round(((float)(totalSkills - missingSkills.size()) / totalSkills) * 100) + "%"
                    : "0%";

            // 요약이 없을 경우 AI로 생성
            String summary = m.getResumeSummary();
            if (summary == null || summary.isBlank()) {
                summary = aiRecommendationService.generateResumeSummary(doc.getFullText());
            }

            return new MatchListResponseDto(
                    resumeEntity.getId(),
                    resumeEntity.getName(),
                    resumeEntity.getPortfolioFileUrl(),
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
}
