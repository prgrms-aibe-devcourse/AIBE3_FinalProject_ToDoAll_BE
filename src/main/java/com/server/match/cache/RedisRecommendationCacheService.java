package com.server.match.cache;

import com.server.ai.service.AiRecommendationService;
import com.server.ai.service.KeywordExtractorService;
import com.server.match.util.RecommendationReasonBuilder;
import com.server.search.document.ResumeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRecommendationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KeywordExtractorService keywordExtractorService;
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

        if (cached instanceof List) {
            log.info("[캐시 HIT] JD 키워드 캐시 — JD {}", jdId);
            return (List<String>) cached;
        } else if (cached != null) {
            log.warn("[캐시 경고] JD 키워드 캐시 형식 불일치 — JD {}, 타입: {}", jdId, cached.getClass().getName());
        }

        log.info("[캐시 MISS] JD 키워드 캐시 — JD {} → AI 호출", jdId);
        List<String> keywords = keywordExtractorService.extractKeywords(description);
        redisTemplate.opsForValue().set(key, keywords, TTL);
        return keywords;
    }

    // 이력서 요약 캐시 조회 또는 생성
    public String getOrGenerateSummary(Long resumeId, String fullText, AiRecommendationService aiService) {
        String key = "summary:" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof String str) {
            log.info("[캐시 HIT] 이력서 요약 캐시 — Resume {}", resumeId);
            return str;
        }

        log.info("[캐시 MISS] 이력서 요약 캐시 — Resume {} → AI 호출", resumeId);
        String result = aiService.generateResumeSummary(fullText);
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }

    // JD + 이력서 조합 추천 사유 캐시 조회 또는 생성
    public String getOrGenerateReason(Long jdId, Long resumeId, String jdDescription, ResumeDocument doc, AiRecommendationService aiService) {
        String key = "reason:jd_" + jdId + ":resume_" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof String str) {
            log.info("[캐시 HIT] 추천 사유 캐시 — JD {}, Resume {}", jdId, resumeId);
            return str;
        }

        log.info("[캐시 MISS] 추천 사유 캐시 — JD {}, Resume {} → AI 호출", jdId, resumeId);
        String result = RecommendationReasonBuilder.buildReason(jdDescription, doc);
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