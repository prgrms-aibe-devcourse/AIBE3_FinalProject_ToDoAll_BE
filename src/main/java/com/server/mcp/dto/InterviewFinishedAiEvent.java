package com.server.mcp.dto;

// 면접 종료 시 AI 요약 생성 이벤트
public record InterviewFinishedAiEvent(
        Long interviewId
) {}
