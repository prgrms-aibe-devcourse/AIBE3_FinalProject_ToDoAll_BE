package com.server.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
        long start = System.currentTimeMillis();

        Map<String, Object> request = Map.of("interviewId", interviewId);
        String url = mcpBaseUrl + generateQuestionsPath;

        try {
            var response = restTemplate.postForEntity(url, request, Void.class);
            long end = System.currentTimeMillis();

            log.info(
                    "[AI-QUESTION] interviewId={} url={} status={} duration={} ms",
                    interviewId, url, response.getStatusCode(), (end - start)
            );
        } catch (RestClientException ex) {
            long end = System.currentTimeMillis();
            log.error(
                    "[AI-QUESTION] interviewId={} url={} 요청 실패 (duration={} ms): {}",
                    interviewId, url, (end - start), ex.getMessage(), ex
            );
        }
    }
}