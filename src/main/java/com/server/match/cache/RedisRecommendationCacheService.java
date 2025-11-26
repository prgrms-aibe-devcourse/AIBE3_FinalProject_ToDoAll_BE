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

    public String getOrGenerateSummary(Long resumeId, String fullText, AiRecommendationService aiService) {
        String key = "summary:" + resumeId;
        Object cached = redisTemplate.opsForValue().get(key);
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
}