package com.server.match.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.server.ai.service.AiRecommendationService;
import com.server.ai.service.KeywordExtractorService;
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
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeSearchService resumeSearchService;
    private final ElasticsearchClient elasticsearchClient;
    private final AiRecommendationService aiRecommendationService;
    private final KeywordExtractorService keywordExtractorService;


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
        float score = MatchScoreCalculator.calculateMatchScore(jd, doc, resume);

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
    public List<ResumeRecommendationDto> recommendResumes(Long jdId) throws IOException {
        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        String queryText = String.join(" ", jd.getRequiredSkillNames()) + " " +
                String.join(" ", jd.getPreferredSkillNames());

        // JD 설명 기반 키워드 추출 (AI 기반)
        List<String> jdKeywords = keywordExtractorService.extractKeywords(jd.getDescription());

        // Java client 사용
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("resume")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("jdId").value(jdId))) // JD 연결
                        .should(s -> s.match(t -> t.field("skills").query(queryText).boost(5.0f)))
                        .should(s -> s.match(t -> t.field("experienceSummary").query(queryText).boost(2.0f)))
                        .should(s -> s.match(t -> t.field("educationSummary").query(queryText).boost(2.0f)))
                        .should(s -> s.match(t -> t.field("certificationSummary").query(queryText).boost(1.5f)))
                        .should(s -> s.match(t -> t.field("activitySummary").query(queryText).boost(1.0f)))
                ))
                .size(10)
                .build();

        SearchResponse<ResumeDocument> response = elasticsearchClient.search(
                searchRequest,
                ResumeDocument.class
        );

        return response.hits().hits().stream()
                .map(hit -> {
                    ResumeDocument doc = hit.source();
                    if (doc == null) return null;

                    Resume resume = resumeRepository.findById(doc.getId()).orElse(null);
                    if (resume == null) return null;

                    boolean exists = matchRepository.existsByJobDescription_IdAndResume_Id(jdId, doc.getId());
                    if (exists) return null;

                    // matchScore 계산 개선
                    float score = MatchScoreCalculator.calculateMatchScoreWithKeywords(jd, doc, resume, jdKeywords);
                    List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

                    String summary = aiRecommendationService.generateResumeSummary(doc.getFullText());

                    String reason = RecommendationReasonBuilder.buildReason(jd.getDescription(), doc);
                    if (reason == null || reason.isBlank()) {
                        reason = "이 JD와 관련된 경력 및 스킬을 보유하고 있습니다.";
                    }

                    Match match = Match.of(jd, resume, LocalDateTime.now(), score, reason, summary, MatchStatus.RECOMMENDED);
                    matchRepository.save(match);

                    return ResumeRecommendationDto.from(resume, doc, score, missingSkills, summary, reason);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ResumeRecommendationDto::matchScore).reversed())
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