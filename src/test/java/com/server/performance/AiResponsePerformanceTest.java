package com.server.performance;

import com.server.ai.service.AiRecommendationService;
import com.server.match.cache.RedisRecommendationCacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


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

        System.out.println("1차 호출 (AI 호출): " + latency1 + "ms");
        System.out.println("2차 호출 (Redis 캐시): " + latency2 + "ms");
        System.out.printf("캐시 적용으로 %dms (%d%%) 개선됨\n", (latency1 - latency2), (100 * (latency1 - latency2) / latency1));

        Assertions.assertEquals(result1, result2);
    }
}
