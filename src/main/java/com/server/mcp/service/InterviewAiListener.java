package com.server.mcp.service;

import com.server.interview.service.InterviewService;
import com.server.mcp.dto.InterviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InterviewAiListener {
    private final InterviewQuestionAiService interviewQuestionAiService;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInterviewCreated(InterviewCreatedEvent event) {
        interviewQuestionAiService.requestAutoQuestionGenerate(
                event.interviewId(),
                event.resumeId(),
                event.jdId()
        );
    }
}
