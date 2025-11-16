package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.*;
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

        // 5. 요청 파싱 및 검증
        for (var q : request.questions()) {
            if (q == null || q.content() == null || q.questionType() == null) {
                throw new ApplicationException(InterviewQuestionErrorCase.INVALID_FIELD);
            }
        }

        // 6. 기존 질문 삭제 후 재삽입 (가장 안정적인 방식)
        questionRepository.deleteByInterviewId(interviewId);

        // 7. 새 질문 삽입
        List<InterviewQuestion> newQuestions = request.questions().stream()
                .map(q -> InterviewQuestion.of(
                        interview,
                        q.questionType(),
                        q.content(),
                        QuestionStatus.PENDING
                ))
                .toList();

        questionRepository.saveAll(newQuestions);
    }
}
