package com.server.mcp.service;

import com.server.mcp.dto.InterviewCreatedAiEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InterviewAiListener {
    private final InterviewQuestionAiService interviewQuestionAiService;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInterviewCreated(InterviewCreatedAiEvent event) {
        interviewQuestionAiService.requestAutoQuestionGenerate(
                event.interviewId()
        );
    }
}
