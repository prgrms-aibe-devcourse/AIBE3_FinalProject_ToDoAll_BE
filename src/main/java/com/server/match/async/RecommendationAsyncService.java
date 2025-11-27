package com.server.match.async;

import com.server.match.cache.RedisRecommendationCacheService;
import com.server.match.service.MatchService;
import com.server.search.dto.ResumeRecommendationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationAsyncService {

    private final MatchService matchService;
    private final RedisRecommendationCacheService cacheService;

    @Async
    public void warmUpRecommendation(Long jdId) {
        try {
            log.info("[Async] 추천 캐시 생성 시작 — JD {}", jdId);
            List<ResumeRecommendationDto> result = matchService.calculateRecommendations(jdId);
            cacheService.saveRecommendations(jdId, result);
            log.info("[Async] 추천 캐시 생성 완료 — JD {}", jdId);
        } catch (Exception e) {
            log.error("[Async] 추천 생성 실패 — JD {}", jdId, e);
        }
    }
}
