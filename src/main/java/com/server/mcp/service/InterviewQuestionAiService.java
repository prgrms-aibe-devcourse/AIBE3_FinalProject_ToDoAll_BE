package com.server.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionAiService {

    private final RestTemplate restTemplate;

    @Value("${mcp.base-url}")
    private String mcpBaseUrl;

    @Value("${mcp.generate-questions-path:/api/ai/interviews/generate-questions}")
    private String generateQuestionsPath;

    public void requestAutoQuestionGenerate(Long interviewId) {
        String url = mcpBaseUrl + generateQuestionsPath;
        var request = Map.of("interviewId", interviewId);

        log.info("[AI-Q] MCP 호출 시작 - URL={}, interviewId={}", url, interviewId);

        try {
            var response = restTemplate.postForEntity(url, request, String.class);
            log.info("[AI-Q] MCP 응답 수신 - status={}, body={}",
                    response.getStatusCode(), response.getBody());

        } catch (Exception e) {
            log.error("[AI-Q] MCP 호출 실패 - interviewId={}, reason={}",
                    interviewId, e.getMessage(), e);
            throw e;
        }
        log.info("[AI-Q] MCP 호출 정상 종료 - interviewId={}", interviewId);
    }
}
