package com.server.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewQuestionAiService {

    private final RestTemplate restTemplate;

    @Value("${mcp.base-url}")
    private String mcpBaseUrl;

    @Value("${mcp.generate-questions-path:/api/ai/interviews/generate-questions}")
    private String generateQuestionsPath;

    public void requestAutoQuestionGenerate(Long interviewId) {
        var request = Map.of("interviewId", interviewId);

        String url = mcpBaseUrl + generateQuestionsPath;

        restTemplate.postForEntity(url, request, Void.class);

        System.out.println("End RequestAutoQuestionGenerate");
    }
}
