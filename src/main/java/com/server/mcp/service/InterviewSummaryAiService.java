package com.server.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

//메인 서버 → MCP 서버 면접 요약 요청 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSummaryAiService {

    private final RestTemplate restTemplate;

    // application.yml 에서 주입
    @Value("${mcp.base-url}")
    private String mcpBaseUrl;
    @Value("${mcp.summary-path:/api/ai/interviews/generate-summary}")
    private String summaryPath;

    //주어진 인터뷰 ID에 대해 AI 요약 생성을 MCP 서버에 요청한다.

    public void requestAutoSummary(Long interviewId) {
        long start = System.currentTimeMillis();

        // 1) MCP 서버에 보낼 요청 바디 구성
        Map<String, Object> request = Map.of(
                "interviewId", interviewId
        );
        String url = mcpBaseUrl + summaryPath;

        // 2) MCP 서버 호출
        try {
            // 2) MCP 서버 호출
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    request,
                    Void.class
            );

            long end = System.currentTimeMillis();

            log.info(
                    "[AI-SUMMARY] interviewId={} url={} status={} duration={} ms",
                    interviewId,
                    url,
                    response.getStatusCode(),
                    (end - start)
            );
        } catch (RestClientException ex) {
            long end = System.currentTimeMillis();

            // 실패 시에도 얼마나 걸렸는지 같이 로깅
            log.error(
                    "[AI-SUMMARY] interviewId={} url={} 요약 요청 실패 (duration={} ms): {}",
                    interviewId,
                    url,
                    (end - start),
                    ex.getMessage(),
                    ex
            );
            }

    }
}
