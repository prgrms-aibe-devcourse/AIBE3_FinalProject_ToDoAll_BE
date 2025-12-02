package com.server.ai;

import com.server.ai.service.AiRecommendationService;
import com.server.match.cache.RedisRecommendationCacheService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@EnabledIfEnvironmentVariable(named = "CI", matches = "false")
public class AiCacheBehaviorTest {

    @Autowired
    private RedisRecommendationCacheService redisCache;

    @Autowired
    private AiRecommendationService aiService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Long resumeId = 42L;
    private final String resumeText = "이력서 내용입니다. Java Spring Redis Kafka 경험 있음";
    private final String redisKey = "summary:" + resumeId;

    @BeforeEach
    void setup() {
        redisTemplate.delete(redisKey); // 캐시 삭제
    }

    @Test
    @Order(1)
    void testCacheMissTriggersAiAndStoresInRedis() {
        Assertions.assertNull(redisTemplate.opsForValue().get(redisKey), "처음엔 캐시에 없는 상태로 진행");

        String summary = redisCache.getOrGenerateSummary(resumeId, resumeText);

        Object cached = redisTemplate.opsForValue().get(redisKey);
        Assertions.assertNotNull(cached, "AI 호출 후 캐시에 저장되어야 함");
        Assertions.assertEquals(summary, cached.toString(), "저장된 캐시값이 반환된 값과 같아야 함");

        log.info("캐시 미스 시 AI 호출 → 캐시 저장 성공");
    }

    @Test
    @Order(2)
    void testCacheHitSkipsAiCall() {
        redisTemplate.opsForValue().set(redisKey, "사전 저장된 요약 결과");

        String result = redisCache.getOrGenerateSummary(resumeId, resumeText);

        Assertions.assertEquals("사전 저장된 요약 결과", result, "캐시에서 바로 불러와야 함");

        log.info("캐시 히트 시 AI 호출 없이 캐시 값 사용 성공");
    }
}