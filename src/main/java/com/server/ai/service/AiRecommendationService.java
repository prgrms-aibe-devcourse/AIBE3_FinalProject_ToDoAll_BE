package com.server.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiRecommendationService {

    private final ChatClient chatClient;

    public AiRecommendationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // 채용공고 설명과 이력서를 기반으로 추천 사유 생성
    public String generateRecommendation(String jdDescription, String resumeFullText) {
        String prompt = """
            너는 채용 담당자야. 아래는 채용공고 설명과 이력서 전체 내용이야.
            이 지원자가 왜 해당 포지션에 적합한지를 2~3문장으로 요약해줘.

            [채용공고 설명]
            %s

            [이력서 본문]
            %s
            """.formatted(jdDescription, resumeFullText);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 추천 사유 생성 실패: {}", e.getMessage(), e);
            return "AI 추천 사유 생성에 실패했습니다.";
        }
    }

    // 이력서 전체를 요약 (핵심 경력 위주)
    public String generateResumeSummary(String resumeFullText) {
        String prompt = """
            아래는 이력서 전체 내용이다.
            중요한 핵심 경력과 강점을 중심으로 4줄 내외로 요약해줘.

            [이력서 본문]
            %s
            """.formatted(resumeFullText);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("이력서 요약 생성 실패: {}", e.getMessage(), e);
            return "이력서 요약 생성에 실패했습니다.";
        }
    }
}

