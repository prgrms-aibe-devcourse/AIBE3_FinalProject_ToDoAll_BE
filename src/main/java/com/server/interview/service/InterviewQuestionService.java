package com.server.interview.service;

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

    @Transactional
    public void updateQuestions(Long interviewId, InterviewQuestionUpdateRequestDto request) {

        // 면접 존재 여부
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 요청 형식 검증
        if (request == null || request.questions() == null) {
            throw new ApplicationException(InterviewQuestionErrorCase.INVALID_FORMAT);
        }

        // 사용자 (토큰 대신 임시)
        User user = userRepository.findById(1L).orElseThrow();

        // 권한 체크
        boolean permission = participantRepository.existsByInterviewIdAndUserId(
                interviewId, user.getId()
        );
        if (!permission) {
            throw new ApplicationException(InterviewQuestionErrorCase.FORBIDDEN);
        }

        // 삭제 처리
        if (request.deleteQuestionIds() != null && !request.deleteQuestionIds().isEmpty()) {

            //기본적으로 Spring Data JPA는 존재하지 않는 ID로 delete를 호출해도 에러를 내지 않고 그냥 무시한다.
            int deleted = questionRepository.deleteByIdsAndInterviewId(
                    request.deleteQuestionIds(), interviewId
            );

            // 요청된 삭제 수 != 실제 삭제 수 -> 잘못된 질문 ID 포함
            if (deleted != request.deleteQuestionIds().size()) {
                throw new ApplicationException(InterviewQuestionErrorCase.INVALID_DELETE_TARGET);
            }
        }

        // 추가 및 수정 처리
        for (var q : request.questions()) {

            if (q.questionId() == null) {
                // 새 질문 추가
                InterviewQuestion newQuestion = InterviewQuestion.of(
                        interview,
                        q.questionType(),
                        q.content(),
                        QuestionStatus.PENDING
                );
                questionRepository.save(newQuestion);
            } else {
                // 기존 질문 수정
                InterviewQuestion existQuestion = questionRepository.findById(q.questionId())
                        .orElseThrow(() -> new ApplicationException(InterviewQuestionErrorCase.INTERVIEW_QUESTION_NOT_FOUND));

                // 질문이 해당 인터뷰에 속하는지 확인
                if (!existQuestion.getInterview().getId().equals(interviewId)) {
                    throw new ApplicationException(InterviewQuestionErrorCase.INTERVIEW_QUESTION_NOT_FOUND);
                }
                existQuestion.update(q.questionType(), q.content());
            }
        }
    }

    public List<InterviewQuestionResponseDto> getQuestions(Long  interviewId) {
        // 인터뷰 존재 체크
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 사용자 조회 (토큰 대신 임시 userId=1)
        User user = userRepository.findById(1L)
                .orElseThrow();

        // 권한 체크 (면접관 + 주최자)
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, user.getId());
        if (!allowed) {
            throw new ApplicationException(InterviewQuestionErrorCase.FORBIDDEN);
        }

        // 질문 조회
        var questions = questionRepository.findAllByInterviewId(interviewId);

        // DTO 변환
        List<InterviewQuestionResponseDto> responseList = questions.stream()
                .map(q -> new InterviewQuestionResponseDto(
                        q.getId(),
                        q.getType().name(),
                        q.getQuestionText(),
                        q.getCreatedAt(),
                        q.getUpdatedAt()
                ))
                .toList();

        return responseList;
    }
}
