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

    // JD 키워드 캐시 삭제
    @DeleteMapping("/keywords/{jdId}")
    public String deleteKeywords(@PathVariable Long jdId) {
        redisRecommendationCacheService.evictKeywords(jdId);
        return "JD 키워드 캐시 삭제 완료: " + jdId;
    }

    // JD 추천 결과 목록 캐시 삭제
    @DeleteMapping("/recommendations/{jdId}")
    public String deleteRecommendationList(@PathVariable Long jdId) {
        redisRecommendationCacheService.evictRecommendationResultList(jdId);
        return "JD 추천 목록 캐시 삭제 완료: " + jdId;
    }

    // JD + Resume → 추천 사유 캐시 삭제
    @DeleteMapping("/reason/{jdId}/resume/{resumeId}")
    public String deleteRecommendationReason(@PathVariable Long jdId, @PathVariable Long resumeId) {
        redisRecommendationCacheService.evictRecommendationReason(jdId, resumeId);
        return "추천 사유 캐시 삭제 완료: JD " + jdId + ", Resume " + resumeId;
    }

    // 이력서 요약 캐시 삭제
    @DeleteMapping("/summary/{resumeId}")
    public String deleteResumeSummary(@PathVariable Long resumeId) {
        redisRecommendationCacheService.evictSummary(resumeId);
        return "이력서 요약 캐시 삭제 완료: " + resumeId;
    }
}
