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

    // JD 지원 + 매칭 등록 
    @Transactional
    public Match applyToJobDescription(MatchRequestDto dto) {
        JobDescription jd = jobDescriptionRepository.findById(dto.jdId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        Resume resume = resumeRepository.findById(dto.resumeId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.RESUME_NOT_FOUND));

        if (matchRepository.existsByJobDescription_IdAndResume_Id(jd.getId(), resume.getId())) {
            throw new ApplicationException(MatchErrorCase.MATCH_ALREADY_EXISTS);
        }

        Match match = Match.ofForApplication(jd, resume);
        matchRepository.save(match);

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

                    // MatchScore 계산 (스킬 + 학력 + 자격증 + 활동)
                    float matchScore = MatchScoreCalculator.calculateMatchScoreWithKeywords(jd, doc, resume, jdKeywords);

                    // ES 점수 반영
                    float esScore = hit.score() != null ? hit.score().floatValue() : 0.0f;

                    // 최종 점수 결과 = MatchScore * 0.7 + ES Score 정규화 * 0.3
                    float normalizedEsScore = response.hits().maxScore() != null && response.hits().maxScore() > 0
                            ? (float) (esScore / response.hits().maxScore())
                            : 0.0f;

                    float finalScore = (matchScore * 0.7f) + (normalizedEsScore * 0.3f);

                    List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

                    String summary = aiRecommendationService.generateResumeSummary(doc.getFullText());

                    String reason = RecommendationReasonBuilder.buildReason(jd.getDescription(), doc);
                    if (reason == null || reason.isBlank()) {
                        reason = "이 JD와 관련된 경력 및 스킬을 보유하고 있습니다.";
                    }

                    Match match = Match.of(jd, resume, LocalDateTime.now(), finalScore, reason, summary, MatchStatus.RECOMMENDED);;
                    matchRepository.save(match);

                    return ResumeRecommendationDto.from(resume, doc, finalScore, missingSkills, summary, reason);
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

        Resume resume = match.getResume();
        ResumeDocument doc = resumeSearchService.find(resume.getId())
                .orElseGet(() -> ResumeDocument.of(resume));

        JobDescription jd = match.getJobDescription();

        // JD 키워드 추출
        List<String> jdKeywords = keywordExtractorService.extractKeywords(jd.getDescription());

        // 누락된 스킬
        List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

        // 전체 기술 수
        int totalSkills = missingSkills.size() + doc.getSkills().size();
        float percentage = totalSkills > 0
                ? (float) (totalSkills - missingSkills.size()) / totalSkills
                : 0f;

        String skillMatchRate = Math.round(percentage * 100) + "%";

        return MatchDetailResponseDto.builder()
                .jdTitle(jd.getTitle())
                .resumeName(resume.getName())
                .matchScore(match.getMatchScore() != null ? match.getMatchScore() : 0.0f)
                .skillMatchRate(skillMatchRate)
                .missingSkills(missingSkills)
                .recommendationReason(match.getRecommendationReason())
                .resumeSummary(match.getResumeSummary())
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