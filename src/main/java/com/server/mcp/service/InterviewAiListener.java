package com.server.mcp.service;

import com.server.mcp.dto.InterviewCreatedAiEvent;
import com.server.mcp.dto.InterviewFinishedAiEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewAiListener {
    private final InterviewQuestionAiService interviewQuestionAiService;
    private final InterviewSummaryAiService interviewSummaryAiService;

    //인터뷰 생성 -> 자동 질문 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInterviewCreated(InterviewCreatedAiEvent event) {
        interviewQuestionAiService.requestAutoQuestionGenerate(
                event.interviewId(),
                event.resumeId(),
                event.jdId()
        );
    }

    //인터뷰 종료 → 요약 자동 생성
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInterviewFinished(InterviewFinishedAiEvent event) {
        log.info("[InterviewAiListener] InterviewFinishedAiEvent 수신 - interviewId={}", event.interviewId());

        interviewSummaryAiService.requestAutoSummary(
                event.interviewId()
        );
    }
}
