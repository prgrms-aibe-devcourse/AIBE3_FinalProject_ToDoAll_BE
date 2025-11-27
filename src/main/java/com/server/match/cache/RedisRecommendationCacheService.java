package com.server.match.cache;

import com.server.ai.service.AiRecommendationService;
import com.server.match.util.RecommendationReasonBuilder;
import com.server.search.document.ResumeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRecommendationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

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

    public String getOrGenerateSummary(Long resumeId, String fullText, AiRecommendationService aiService) {
        String key = "summary:" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("캐시 HIT for key {}", key);
            return cached.toString();
        }
        log.info("캐시 MISS for key {}, calling AI", key);

        if (cached instanceof String str) {
            return str;
        }

        String result = aiService.generateResumeSummary(fullText);
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }

    public String getOrGenerateReason(Long jdId, Long resumeId, String jdDescription, ResumeDocument doc, AiRecommendationService aiService) {
        String key = "reason:jd_" + jdId + ":resume_" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof String str) {
            return str;
        }

        String result = RecommendationReasonBuilder.buildReason(jdDescription, doc);
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }

    // 테스트에서 사용하기 위한 Redis 캐시 삭제 메서드
    public void evictSummary(Long resumeId) {
        String key = "summary:" + resumeId;
        redisTemplate.delete(key);
    }
}