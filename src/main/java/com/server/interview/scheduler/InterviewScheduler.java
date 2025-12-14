package com.server.interview.scheduler;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewEvaluationRepository;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewQuestionRepository;
import com.server.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewScheduler {

    private final InterviewRepository interviewRepository;
    private final InterviewNoteRepository interviewNoteRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewEvaluationRepository interviewEvaluationRepository;

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    @Transactional
    public void autoStartInterview(){
        LocalDateTime now = LocalDateTime.now().plusHours(9);
        interviewRepository.updateInProgress(now);
    }

    @Scheduled(fixedRate = 60000, initialDelay = 60000) // 1분마다 검사
    @Transactional
    public void autoDeleteInterview() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(1);
        List<Long> oldInterviewIds = interviewRepository.findInterviewIdsOlderThan(threshold);

        if (oldInterviewIds.isEmpty()) return;

        log.info("Deleting {} old interviews...", oldInterviewIds.size());

        for (Long interviewId : oldInterviewIds) {
            try {
                // organizer 권한 체크를 우회해야 하므로 service 수정 필요
                autoDeleteInterviewById(interviewId);
            } catch (Exception e) {
                log.error("Interview {} auto-delete failed: {}", interviewId, e.getMessage());
            }
        }
    }

    @Transactional
    public void autoDeleteInterviewById(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 연관 노트 엔티티 조회
        interviewNoteRepository.findByInterviewId(interviewId)
                .ifPresent(interviewNoteRepository::delete);

        // 질문 삭제
        interviewQuestionRepository.deleteByInterviewId(interviewId);

        // 평가 삭제
        interviewEvaluationRepository.deleteByInterviewId(interviewId);

        // 최종 인터뷰 삭제
        interviewRepository.delete(interview);

        log.info("Auto-deleted interview {}", interviewId);
    }


}
