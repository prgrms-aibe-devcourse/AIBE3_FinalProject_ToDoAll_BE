package com.server.match.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.server.ai.service.AiRecommendationService;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.cache.RedisRecommendationCacheService;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.match.util.MatchScoreCalculator;
import com.server.resume.domain.Resume;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.search.document.ResumeDocument;
import com.server.search.dto.ResumeRecommendationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationCoreService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final MatchRepository matchRepository;
    private final RedisRecommendationCacheService redisRecommendationCacheService;
    private final ElasticsearchClient elasticsearchClient;
    private final AiRecommendationService aiRecommendationService;

    public List<ResumeRecommendationDto> calculateRecommendations(Long jdId) throws IOException {
        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        String queryText = String.join(" ", jd.getRequiredSkillNames()) + " " +
                String.join(" ", jd.getPreferredSkillNames());

        // JD 설명 기반 키워드 추출 (AI 기반)
        List<String> jdKeywords = redisRecommendationCacheService.getOrGenerateKeywords(jdId, jd.getDescription());

        // Java client 사용
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("resume")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("jdId").value(jdId)))
                        .should(s -> s.match(t -> t.field("skills").query(queryText).boost(5.0f)))
                        .should(s -> s.match(t -> t.field("experienceSummary").query(queryText).boost(2.0f)))
                        .should(s -> s.match(t -> t.field("educationSummary").query(queryText).boost(2.0f)))
                        .should(s -> s.match(t -> t.field("certificationSummary").query(queryText).boost(1.5f)))
                        .should(s -> s.match(t -> t.field("activitySummary").query(queryText).boost(1.0f)))
                ))
                .size(10)
                .build();

        SearchResponse<ResumeDocument> response = elasticsearchClient.search(searchRequest, ResumeDocument.class);

        return response.hits().hits().stream()
                .map(hit -> {
                    ResumeDocument doc = hit.source();
                    if (doc == null) return null;

                    // N+1 문제 발생하지 않도록 Fetch Join 적용
                    Resume resume = resumeRepository.findWithEssentialDetailsById(doc.getId())
                            .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

                    Optional<Match> existingMatch = matchRepository.findByJobDescription_IdAndResume_Id(jdId, doc.getId());
                    if (existingMatch.isPresent()) {
                        MatchStatus status = existingMatch.get().getStatus();
                        // APPLIED, BOOKMARK, RECOMMENDED 등은 포함 (나머지 상태는 필터)
                        if (status == MatchStatus.CONFIRMED || status == MatchStatus.REJECTED || status == MatchStatus.HOLD) {
                            return null;
                        }
                    }

                    // MatchScore 계산 (스킬 + 학력 + 자격증 + 활동)
                    float matchScore = MatchScoreCalculator.calculateMatchScoreWithKeywords(jd, doc, resume, jdKeywords);

                    // ES 점수 반영
                    float esScore = hit.score() != null ? hit.score().floatValue() : 0.0f;

                    // 최종 점수 결과 = MatchScore * 0.7 + ES Score 정규화 * 0.3
                    float normalizedEsScore = response.hits().maxScore() != null && response.hits().maxScore() > 0
                            ? (float) (esScore / response.hits().maxScore()) : 0.0f;
                    float finalScore = (matchScore * 0.7f) + (normalizedEsScore * 0.3f);

                    List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

                    // Redis 캐시 적용 => 이력서 요약이 캐시에 없을 때만 AI 호출
                    String summary = redisRecommendationCacheService.getOrGenerateSummary(doc.getId(), doc.getFullText(), aiRecommendationService);

                    // Redis 캐시 적용 => JD+이력서 조합에 따른 추천 사유가 캐시에 없을 때만 AI 호출
                    String reason = redisRecommendationCacheService.getOrGenerateReason(jdId, doc.getId(), jd.getDescription(), doc, aiRecommendationService);

                    if (reason == null || reason.isBlank()) {
                        reason = "이 JD와 관련된 경력 및 스킬을 보유하고 있습니다.";
                    }

                    return ResumeRecommendationDto.from(resume, doc, finalScore, missingSkills, summary, reason);
                })
                .filter(dto -> dto != null)
                .sorted(Comparator.comparing(ResumeRecommendationDto::matchScore).reversed())
                .toList();
    }
}