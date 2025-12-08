package com.server.mcp.service;

import com.server.mcp.dto.InterviewCreatedAiEvent;
import com.server.mcp.dto.InterviewFinishedAiEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewAiListener {
    private final AiInterviewService aiInterviewService;
    private final InterviewSummaryAiService interviewSummaryAiService;

    //인터뷰 생성 -> 자동 질문 생성
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleInterviewCreated(InterviewCreatedAiEvent event) {
        log.info("[AI-LISTENER] InterviewCreatedAiEvent 수신 - interviewId={}", event.interviewId());

        aiInterviewService.generateQuestions(event.interviewId());

        log.info("[AI-LISTENER] 질문 생성 요청 완료 - interviewId={}", event.interviewId());
    }

    //인터뷰 종료 → 요약 자동 생성
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleInterviewFinished(InterviewFinishedAiEvent event) {
        log.info("[InterviewAiListener] InterviewFinishedAiEvent 수신 - interviewId={}", event.interviewId());

        interviewSummaryAiService.generateSummary(event.interviewId());

        log.info("[AI-LISTENER] 요약 생성 요청 완료 - interviewId={}", event.interviewId());
    }
}
