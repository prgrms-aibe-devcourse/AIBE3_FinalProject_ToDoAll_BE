package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewEvaluation;
import com.server.interview.domain.InterviewResult;
import com.server.interview.dto.InterviewEvaluationCreateRequestDto;
import com.server.interview.dto.InterviewEvaluationCreateResponseDto;
import com.server.interview.dto.InterviewEvaluationSearchResponseDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewEvaluationErrorCase;
import com.server.interview.repository.InterviewEvaluationRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewEvaluationService {

    private final InterviewRepository interviewRepository;
    private final InterviewEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final InterviewParticipantRepository participantRepository;

    private Interview getInterview(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
    }

    private User getUser() {
        // TODO: JWT 인증 후 실제 userId 사용하도록 수정
        return userRepository.findById(1L)
                .orElseThrow();
    }

    private void checkPermission(Long interviewId, Long userId) {
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, userId);
        if (!allowed) {
            throw new ApplicationException(InterviewEvaluationErrorCase.FORBIDDEN);
        }
    }

    // 인터뷰 평가 중복 여부 검사 (분리된 메서드 호출)
    private void validateEvaluationNotExists(Long interviewId) {
        if (evaluationRepository.existsByInterviewId(interviewId)) {
            throw new ApplicationException(
                    InterviewEvaluationErrorCase.EXIST_EVALUATION
            );
        }
    }

    @Transactional
    public InterviewEvaluationCreateResponseDto create(
            Long interviewId,
            InterviewEvaluationCreateRequestDto request
    ) {

        // 공통 검증
        Interview interview = getInterview(interviewId);
        User evaluator = getUser();
        checkPermission(interviewId, evaluator.getId());
        validateEvaluationNotExists(interviewId);

        // 정적 팩토리 메서드로 객체 생성
        InterviewEvaluation evaluation = InterviewEvaluation.of(
                interview,
                evaluator,
                request.scoreTech(),
                request.scoreComm(),
                request.scoreOverall(),
                request.comment(),
                InterviewResult.PENDING
        );

        // 저장
        evaluationRepository.save(evaluation);

        // 응답 반환
        return new InterviewEvaluationCreateResponseDto(
                evaluation.getId(),
                evaluation.getScoreTech(),
                evaluation.getScoreComm(),
                evaluation.getScoreOverall(),
                evaluation.getComment()
        );
    }

    public InterviewEvaluationSearchResponseDto getEvaluations(Long interviewId) {

        // 공통 검증
        getInterview(interviewId);
        User evaluator = getUser();
        checkPermission(interviewId, evaluator.getId());

        // 평가 조회
        InterviewEvaluation evaluation = evaluationRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ApplicationException(
                        InterviewEvaluationErrorCase.INTERVIEW_EVALUATION_NOT_FOUND
                ));

        // 응답 DTO 변환
        return new InterviewEvaluationSearchResponseDto(
                evaluation.getId(),
                evaluation.getScoreTech(),
                evaluation.getScoreComm(),
                evaluation.getScoreOverall(),
                evaluation.getComment(),
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt()
        );
    }
}
