package com.server.match.service;

import com.server.ai.service.AiRecommendationService;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.*;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.match.util.MatchScoreCalculator;
import com.server.match.util.RecommendationReasonBuilder;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.search.document.ResumeDocument;
import com.server.search.dto.ResumeRecommendationDto;
import com.server.search.repository.ResumeSearchRepository;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeSearchService resumeSearchService;
    private final ElasticsearchOperations elasticsearchOperations;
    private final AiRecommendationService aiRecommendationService;
    private final ResumeSearchRepository resumeSearchRepository;

    // 매칭 등록 (지원자 직접 지원)
    @Transactional
    public Match registerMatch(MatchRequestDto dto) {
        JobDescription jd = jobDescriptionRepository.findById(dto.jdId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        Resume resume = resumeRepository.findById(dto.resumeId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.RESUME_NOT_FOUND));

        if (matchRepository.existsByJobDescription_IdAndResume_Id(jd.getId(), resume.getId())) {
            throw new ApplicationException(MatchErrorCase.MATCH_ALREADY_EXISTS);
        }

        // 이력서 색인 or 조회
        ResumeDocument doc = resumeSearchService.find(resume.getId())
                .orElseGet(() -> {
                    resumeSearchService.index(resume);
                    return ResumeDocument.of(resume);
                });

        // AI 추천 사유 + 요약
        String recommendation = aiRecommendationService.generateRecommendation(
                jd.getDescription(), doc.getFullText()
        );

        String resumeSummary = aiRecommendationService.generateResumeSummary(doc.getFullText());

        // 매칭 점수
        float score = MatchScoreCalculator.calculateMatchScore(jd, doc);

        Match match = Match.of(
                jd,
                resume,
                LocalDateTime.now(),
                score,
                recommendation,
                resumeSummary,
                MatchStatus.APPLIED
        );

        matchRepository.save(match);
        resumeSearchService.index(resume);

        return match;
    }

    // JD 기반 추천 이력서 자동 매칭
    @Transactional
    public List<ResumeRecommendationDto> recommendResumes(Long jdId) {
        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        String queryText = jd.getDescription() + " " + String.join(" ", jd.getRequiredSkillNames());

        Criteria criteria = new Criteria("fullText").matches(queryText)
                .and(new Criteria("jdId").is(jd.getId()));
        Query query = new CriteriaQuery(criteria, PageRequest.of(0, 10));

        SearchHits<ResumeDocument> hits = elasticsearchOperations.search(query, ResumeDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> {
                    ResumeDocument doc = hit.getContent();
                    Resume resume = resumeRepository.findById(doc.getId()).orElse(null);
                    if (resume == null) return null;

                    boolean exists = matchRepository.existsByJobDescription_IdAndResume_Id(jd.getId(), doc.getId());
                    if (exists) return null;

                    float score = MatchScoreCalculator.calculateMatchScore(jd, doc);
                    List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

                    System.out.println(">>> JD 필수 스킬: " + jd.getRequiredSkillNames());
                    System.out.println(">>> 이력서 보유 스킬: " + doc.getSkills());
                    System.out.println(">>> 계산된 missingSkills: " + missingSkills);
                    System.out.println(">>> 계산된 matchScore: " + score);

                    String summary = aiRecommendationService.generateResumeSummary(doc.getFullText());

                    String reason = RecommendationReasonBuilder.buildReason(jd.getDescription(), doc);
                    if (reason == null || reason.isBlank()) {
                        reason = "이 JD와 관련된 경력 및 스킬을 보유하고 있습니다.";
                    }

                    Match match = Match.of(
                            jd,
                            resume,
                            LocalDateTime.now(),
                            score,
                            reason,
                            summary,
                            MatchStatus.RECOMMENDED
                    );
                    matchRepository.save(match);

                    return ResumeRecommendationDto.from(resume, doc, score, missingSkills, summary, reason);
                })
                .filter(dto -> dto != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MatchListResponseDto> getMatchedResumesPaged(MatchSearchCondition condition, Pageable pageable) {
        return matchRepository.searchMatches(condition, pageable);
    }

    @Transactional(readOnly = true)
    public MatchDetailResponseDto getMatchDetail(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        return MatchDetailResponseDto.builder()
                .jdTitle(match.getJobDescription().getTitle())
                .resumeName(match.getResume().getName())
                .matchScore(match.getMatchScore() != null ? match.getMatchScore() : 0.0f)
                .skillMatchRate("78%") // TODO: 실제 계산 도입 예정
                .missingSkills(List.of("Redis", "Kafka")) // TODO: 추후 자동 추출
                .recommendationReason(match.getRecommendationReason())
                .resumeSummary(match.getResumeSummary())
                .jdSummary(null)
                .build();
    }

    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchStatus newStatus) {
        if (newStatus == null) {
            throw new ApplicationException(MatchErrorCase.MATCH_INVALID_STATUS);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        match.updateStatus(newStatus);
        return new MatchResponseDto(match.getId(), match.getStatus());
    }
}