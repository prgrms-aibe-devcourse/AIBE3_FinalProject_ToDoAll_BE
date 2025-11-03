package com.server.interview.adapter;

import com.server.interview.port.InterviewAiPort;
import org.springframework.stereotype.Component;

/**
 * MCP 연동 어댑터
 * - 실제로 AI 엔진(MCP)에 요청을 보내는 구현체
 * - InterviewAiPort의 구현 클래스
 */
@Component
public class McpInterviewAdapter implements InterviewAiPort {

    @Override
    public String generateInterviewQuestions(String jdDescription, String resumeContent) {
        // TODO: MCP API 호출로 질문 생성 구현
        return "예상 질문 1, 예상 질문 2...";
    }

    @Override
    public String summarizeInterview(String fullTranscript) {
        // TODO: MCP API 호출로 요약 결과 받기 구현
        return "면접 요약 결과입니다.";
    }
}
