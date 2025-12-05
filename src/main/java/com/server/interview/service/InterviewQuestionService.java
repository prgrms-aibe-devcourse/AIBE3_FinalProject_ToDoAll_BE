package com.server.interview.service;

import com.server.global.auth.AuthUtils;
import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewQuestion;
import com.server.interview.domain.QuestionStatus;
import com.server.interview.dto.InterviewQuestionResponseDto;
import com.server.interview.dto.InterviewQuestionUpdateRequestDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewQuestionErrorCase;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewQuestionRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewQuestionService {

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository participantRepository;
    private final InterviewQuestionRepository questionRepository;
    private final UserRepository userRepository;

    // 공통 로직
    private Interview getInterview(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
    }

    private InterviewQuestion getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ApplicationException(InterviewQuestionErrorCase.INTERVIEW_QUESTION_NOT_FOUND));
    }

    private User getUser() {
        Long userId = AuthUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow();
    }

    private void checkPermission(Long interviewId, Long userId) {
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, userId);
        if (!allowed) {
            throw new ApplicationException(InterviewQuestionErrorCase.FORBIDDEN);
        }
    }

    private void validateQuestionBelongsToInterview(InterviewQuestion question, Long interviewId) {
        if (!question.getInterview().getId().equals(interviewId)) {
            throw new ApplicationException(InterviewQuestionErrorCase.INTERVIEW_QUESTION_NOT_FOUND);
        }
    }

    // 질문 리스트 조회
    public List<InterviewQuestionResponseDto> getQuestions(Long interviewId) {

        getInterview(interviewId);  // 인터뷰 존재 확인
        User user = getUser();
        checkPermission(interviewId, user.getId());

        List<InterviewQuestion> questions = questionRepository.findAllByInterviewId(interviewId);

        return questions.stream()
                .map(q -> new InterviewQuestionResponseDto(
                        q.getId(),
                        q.getType().name(),
                        q.getQuestionText(),
                        q.getCreatedAt(),
                        q.getUpdatedAt(),
                        q.isChecked()
                ))
                .toList();
    }

    // 질문 수정 / 추가 / 삭제
    @Transactional
    public void updateQuestions(Long interviewId, InterviewQuestionUpdateRequestDto request) {

        Interview interview = getInterview(interviewId);
        User user = getUser();
        checkPermission(interviewId, user.getId());

        // 요청 형식 검증
        if (request == null || request.questions() == null) {
            throw new ApplicationException(InterviewQuestionErrorCase.INVALID_FORMAT);
        }

        // 삭제 처리
        if (request.deleteQuestionIds() != null && !request.deleteQuestionIds().isEmpty()) {

            int deleted = questionRepository.deleteByIdsAndInterviewId(
                    request.deleteQuestionIds(), interviewId
            );

            if (deleted != request.deleteQuestionIds().size()) {
                throw new ApplicationException(InterviewQuestionErrorCase.INVALID_DELETE_TARGET);
            }
        }

        // 추가/수정 처리
        for (var q : request.questions()) {

            // 새로운 질문
            if (q.questionId() == null) {
                InterviewQuestion newQuestion = InterviewQuestion.of(
                        interview,
                        q.questionType(),
                        q.content(),
                        QuestionStatus.PENDING,
                        false
                );
                questionRepository.save(newQuestion);
            }

            // 기존 질문 수정
            else {
                InterviewQuestion existQuestion = getQuestion(q.questionId());

                validateQuestionBelongsToInterview(existQuestion, interviewId);

                existQuestion.update(q.questionType(), q.content());
            }
        }
    }

    // 질문 체크 토글
    @Transactional
    public void toggleCheck(Long interviewId, Long questionId) {

        InterviewQuestion question = getQuestion(questionId);
        validateQuestionBelongsToInterview(question, interviewId);

        User user = getUser();
        checkPermission(interviewId, user.getId());

        question.toggleCheck();
    }

    @Transactional
    public void updateQuestionsBySystem(Long interviewId, InterviewQuestionUpdateRequestDto request) {
        // 인터뷰 존재 확인 (공통 메서드 재사용)
        Interview interview = getInterview(interviewId);

        // 형식 검증
        if (request == null || request.questions() == null) {
            throw new ApplicationException(InterviewQuestionErrorCase.INVALID_FORMAT);
        }

        // 삭제 처리 (기존 로직 그대로 사용, 권한 체크만 제거)
        if (request.deleteQuestionIds() != null && !request.deleteQuestionIds().isEmpty()) {

            int deleted = questionRepository.deleteByIdsAndInterviewId(
                    request.deleteQuestionIds(), interviewId
            );

            if (deleted != request.deleteQuestionIds().size()) {
                throw new ApplicationException(InterviewQuestionErrorCase.INVALID_DELETE_TARGET);
            }
        }

        // 추가 / 수정 처리
        for (var q : request.questions()) {

            // 새 질문 추가
            if (q.questionId() == null) {
                InterviewQuestion newQuestion = InterviewQuestion.of(
                        interview,
                        q.questionType(),
                        q.content(),
                        QuestionStatus.PENDING,
                        false // isChecked 기본값
                );
                questionRepository.save(newQuestion);
            }

            // 기존 질문 수정
            else {
                InterviewQuestion existQuestion = getQuestion(q.questionId());

                // 이 인터뷰에 속한 질문인지 검증
                validateQuestionBelongsToInterview(existQuestion, interviewId);

                // 타입/내용만 업데이트
                existQuestion.update(q.questionType(), q.content());
            }
        }
    }
}
