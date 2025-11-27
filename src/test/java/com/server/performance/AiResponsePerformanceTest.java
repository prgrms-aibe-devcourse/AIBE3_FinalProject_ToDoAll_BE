package com.server.performance;

import com.server.ai.service.AiRecommendationService;
import com.server.match.cache.RedisRecommendationCacheService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@EnabledIfEnvironmentVariable(named = "CI", matches = "false")
@SpringBootTest
@ActiveProfiles("test")
public class AiResponsePerformanceTest {

    @Autowired
    private RedisRecommendationCacheService redisCache;

    @Autowired
    private AiRecommendationService aiService;

    @Test
    void measureCacheVsNoCacheLatency() {
        Long resumeId = 100L;
        String resumeText = "이력서 내용입니다. Spring, Java, Redis, Kafka 경험 있음";

        redisCache.evictSummary(resumeId); // 캐시 제거

        // 1차 호출 (실제 AI 호출)
        long start1 = System.nanoTime();
        String result1 = redisCache.getOrGenerateSummary(resumeId, resumeText, aiService);
        long end1 = System.nanoTime();

        // 2차 호출 (캐시 HIT)
        long start2 = System.nanoTime();
        String result2 = redisCache.getOrGenerateSummary(resumeId, resumeText, aiService);
        long end2 = System.nanoTime();

        long latency1 = (end1 - start1) / 1_000_000;
        long latency2 = (end2 - start2) / 1_000_000;

        log.info("1차 호출 (AI 호출): " + latency1 + "ms");
        log.info("2차 호출 (Redis 캐시): " + latency2 + "ms");
        log.info("캐시 적용으로 {}ms ({}%) 개선됨", latency1 - latency2, (100 * (latency1 - latency2) / latency1));

        Assertions.assertEquals(result1, result2);
    }
}
