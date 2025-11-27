package com.server.match.controller;

import com.server.match.cache.RedisRecommendationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/dev/redis")
public class RedisDevController {

    private final RedisRecommendationCacheService redisRecommendationCacheService;

    @DeleteMapping("/keywords/{jdId}")
    public String deleteKeywords(@PathVariable Long jdId) {
        redisRecommendationCacheService.evictKeywords(jdId);
        return "키워드 캐시 삭제 완료: " + jdId;
    }
}
