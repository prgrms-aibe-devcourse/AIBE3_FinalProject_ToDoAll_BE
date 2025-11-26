package com.server.search.batch;

import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeIndexScheduler {

    private final ResumeSearchService resumeSearchService;

    // 매일 새벽 3시에 전체 이력서 색인
    @Scheduled(cron = "0 0 3 * * *")  // 매일 03:00 AM
    public void indexResumesDaily() {
        log.info("[이력서 색인 배치] 시작");
        resumeSearchService.indexAll();
        log.info("[이력서 색인 배치] 완료");
    }

    // @Scheduled(fixedRate = 600_000) // 10분마다 (개발 중 테스트용)
    public void indexResumesEvery10Min() {
        log.info("[테스트용 색인 배치] 시작");
        resumeSearchService.indexAll();
        log.info("[테스트용 색인 배치] 완료");
    }
}
