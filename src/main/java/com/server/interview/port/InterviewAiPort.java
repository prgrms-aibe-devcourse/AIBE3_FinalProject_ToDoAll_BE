package com.server.interview.port;

/**
 * InterviewAiPort
 * - 인터뷰 도메인에서 AI 기능을 추상화한 포트 인터페이스
 * - 면접 질문 생성, 요약, 분석 등 AI 기능 호출 시 사용
 */
public interface InterviewAiPort {

    // AI에게 면접 질문을 생성 요청
    String generateInterviewQuestions(String jdDescription, String resumeContent);

    // AI에게 면접 내용을 요약 요청
    String summarizeInterview(String fullTranscript);
}
