package com.server.mcp.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewQuestionAiService {

    private final RestTemplate restTemplate;

    public void requestAutoQuestionGenerate(Long interviewId, Long resumeId, Long jdId) {
        var request = Map.of(
                "interviewId", interviewId,
                "resumeId", resumeId,
                "jdId", jdId
        );

        restTemplate.postForEntity(
                "http://localhost:8090/api/ai/interviews/generate-questions",
                request,
                Void.class
        );
        System.out.println("End RequestAutoQuestionGenerate");
    }
}