package com.server.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

//메인 서버 → MCP 서버 면접 요약 요청 서비스

@Service
@RequiredArgsConstructor
public class InterviewSummaryAiService {

    private final RestTemplate restTemplate;

    //주어진 인터뷰 ID에 대해 AI 요약 생성을 MCP 서버에 요청한다.

    public void requestAutoSummary(Long interviewId) {
        // 1) MCP 서버에 보낼 요청 바디 구성
        var request = Map.of(
                "interviewId", interviewId
        );

        // 2) MCP 서버 호출
        restTemplate.postForEntity(
                "http://localhost:8090/api/ai/interviews/generate-summary",
                request,
                Void.class
        );

        // 3) 디버깅용 로그
        System.out.println("[AI] RequestAutoSummary interviewId=" + interviewId);
    }
}
