package com.server.match.cache;

import com.server.ai.service.AiRecommendationService;
import com.server.ai.service.KeywordExtractorService;
import com.server.match.util.RecommendationReasonBuilder;
import com.server.search.document.ResumeDocument;
import com.server.search.domain.JdKeyword;
import com.server.search.domain.RecommendationReason;
import com.server.search.domain.ResumeSummary;
import com.server.search.repository.JdKeywordRepository;
import com.server.search.repository.RecommendationReasonRepository;
import com.server.search.repository.ResumeSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRecommendationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KeywordExtractorService keywordExtractorService;
    private final AiRecommendationService aiRecommendationService;

    private final JdKeywordRepository jdKeywordRepository;
    private final ResumeSummaryRepository resumeSummaryRepository;
    private final RecommendationReasonRepository recommendationReasonRepository;

    private static final Duration TTL = Duration.ofDays(30);

    public boolean existsRecommendationFor(Long jdId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("recommend:jd_" + jdId));
    }

    public void saveRecommendations(Long jdId, Object data) {
        redisTemplate.opsForValue().set("recommend:jd_" + jdId, data, TTL);
    }

    @SuppressWarnings("unchecked")
    public <T> T getRecommendations(Long jdId) {
        return (T) redisTemplate.opsForValue().get("recommend:jd_" + jdId);
    }

    // JD 설명 기반 키워드 캐시 조회 또는 생성
    public List<String> getOrGenerateKeywords(Long jdId, String description) {
        String key = "keywords:jd_" + jdId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof List<?> list) {
            log.info("[캐시 HIT] JD 키워드 — JD {}", jdId);
            return (List<String>) list;
        }

        // DB 조회
        Optional<JdKeyword> fromDb = jdKeywordRepository.findById(jdId);
        if (fromDb.isPresent()) {
            List<String> keywords = fromDb.get().getKeywords();
            redisTemplate.opsForValue().set(key, keywords, TTL);
            return keywords;
        }

        // AI 호출
        log.info("[캐시 MISS] JD 키워드 — JD {} → AI 호출", jdId);
        List<String> keywords = keywordExtractorService.extractKeywords(description);

        // DB + 캐시 저장
        jdKeywordRepository.save(JdKeyword.of(jdId, keywords));
        redisTemplate.opsForValue().set(key, keywords, TTL);
        return keywords;
    }

    // 이력서 요약 캐시 조회 또는 생성
    public String getOrGenerateSummary(Long resumeId, String fullText) {
        String key = "summary:" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof String str) {
            log.info("[캐시 HIT] 이력서 요약 — Resume {}", resumeId);
            return str;
        }

        // DB 조회
        Optional<ResumeSummary> fromDb = resumeSummaryRepository.findById(resumeId);
        if (fromDb.isPresent()) {
            String summary = fromDb.get().getSummary();
            redisTemplate.opsForValue().set(key, summary, TTL);
            return summary;
        }

        // AI 호출
        log.info("[캐시 MISS] 이력서 요약 — Resume {} → AI 호출", resumeId);
        String result = aiRecommendationService.generateResumeSummary(fullText);

        resumeSummaryRepository.save(ResumeSummary.of(resumeId, result));
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }

    // JD + 이력서 조합 추천 사유 캐시 조회 또는 생성
    public String getOrGenerateReason(Long jdId, Long resumeId, String jdDescription, ResumeDocument doc) {
        String key = "reason:jd_" + jdId + ":resume_" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof String str) {
            log.info("[캐시 HIT] 추천 사유 — JD {}, Resume {}", jdId, resumeId);
            return str;
        }

        // DB 조회
        Optional<RecommendationReason> fromDb = recommendationReasonRepository.findByJdIdAndResumeId(jdId, resumeId);
        if (fromDb.isPresent()) {
            String reason = fromDb.get().getReason();
            redisTemplate.opsForValue().set(key, reason, TTL);
            return reason;
        }

        // AI 호출
        log.info("[캐시 MISS] 추천 사유 — JD {}, Resume {} → AI 호출", jdId, resumeId);
        String result = RecommendationReasonBuilder.buildReason(jdDescription, doc);

        recommendationReasonRepository.save(RecommendationReason.of(jdId, resumeId, result));
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }

    // 테스트에서 사용하기 위한 Redis 캐시 삭제 메서드
    public void evictSummary(Long resumeId) {
        String key = "summary:" + resumeId;
        redisTemplate.delete(key);
    }

    // JD 키워드 캐시 삭제용 메서드
    public void evictKeywords(Long jdId) {
        String key = "keywords:jd_" + jdId;
        redisTemplate.delete(key);
    }
}