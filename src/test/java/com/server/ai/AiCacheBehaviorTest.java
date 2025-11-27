package com.server.ai;

import com.server.ai.service.AiRecommendationService;
import com.server.match.cache.RedisRecommendationCacheService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(AiCacheBehaviorTest.Config.class)
public class AiCacheBehaviorTest {

    @Mock
    private AiRecommendationService aiRecommendationService;

    @Autowired
    private RedisRecommendationCacheService redisRecommendationCacheService;

    @Test
    void testCachingBehavior() {
        String resumeText = "이력서 내용입니다.";
        Long resumeId = 1L;

        when(aiRecommendationService.generateResumeSummary(resumeText))
                .thenReturn("요약 결과");

        String firstCall = redisRecommendationCacheService.getOrGenerateSummary(
                resumeId, resumeText, aiRecommendationService);

        String secondCall = redisRecommendationCacheService.getOrGenerateSummary(
                resumeId, resumeText, aiRecommendationService);

        assertEquals(firstCall, secondCall);
        verify(aiRecommendationService, times(1)).generateResumeSummary(resumeText);
    }

    @TestConfiguration
    static class Config {
        @Bean
        public RedisRecommendationCacheService redisRecommendationCacheService(
                org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
            return new RedisRecommendationCacheService(redisTemplate);
        }
    }
}