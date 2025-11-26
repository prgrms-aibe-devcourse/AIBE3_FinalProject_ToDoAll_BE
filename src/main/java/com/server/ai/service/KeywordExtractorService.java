package com.server.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractorService {

    private final ChatClient chatClient;

    // 채용공고에서 AI를 통해 주요 키워드(기술, 자격증 등) 추출
    @Cacheable(value = "ai_keywords", key = "#jdDescription") // 성능 개선을 위한 Redis Cache 적용
    public List<String> extractKeywords(String jdDescription) {
        String prompt = """
            아래 채용 공고 설명에서 관련 기술 스택, 자격증, 사용 언어, 프레임워크, 도구 등을 키워드로 추출해줘.
            10개 이하의 핵심 키워드만 추출하고, 쉼표로 구분해줘. 예: Java, Spring, Redis, Kafka, 정보처리기사

            [채용공고 설명]
            %s
            """.formatted(jdDescription);

        try {
            String rawResult = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Java, Spring, Kafka로 되어있는걸  ["java", "spring", "kafka"] 으로 변환
            return Arrays.stream(rawResult.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("JD 키워드 추출 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }
}