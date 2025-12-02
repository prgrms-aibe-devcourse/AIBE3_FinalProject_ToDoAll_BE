//package com.server.interview.service;
//
//import com.server.global.exception.ApplicationException;
//import com.server.interview.domain.Interview;
//import com.server.interview.domain.InterviewQuestion;
//import com.server.interview.domain.QuestionType;
//import com.server.interview.dto.InterviewQuestionUpdateRequestDto;
//import com.server.interview.exception.InterviewErrorCase;
//import com.server.interview.exception.InterviewQuestionErrorCase;
//import com.server.interview.repository.InterviewParticipantRepository;
//import com.server.interview.repository.InterviewQuestionRepository;
//import com.server.interview.repository.InterviewRepository;
//import com.server.user.domain.User;
//import com.server.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class InterviewQuestionServiceTest {
//
//    private InterviewQuestionService interviewQuestionService;
//
//    private InterviewRepository interviewRepository;
//    private InterviewParticipantRepository participantRepository;
//    private InterviewQuestionRepository questionRepository;
//    private UserRepository userRepository;
//
//    @BeforeEach
//    void setUp() {
//
//        interviewRepository = Mockito.mock(InterviewRepository.class);
//        participantRepository = Mockito.mock(InterviewParticipantRepository.class);
//        questionRepository = Mockito.mock(InterviewQuestionRepository.class);
//        userRepository = Mockito.mock(UserRepository.class);
//
//        interviewQuestionService = new InterviewQuestionService(
//                interviewRepository,
//                participantRepository,
//                questionRepository,
//                userRepository
//        );
//    }
//
//    // -------------------------------------------------------
//    // Helper Methods
//    // -------------------------------------------------------
//    private User mockUser(Long id) {
//        User user = mock(User.class);
//        when(user.getId()).thenReturn(id);
//        ReflectionTestUtils.setField(user, "id", id);
//        return user;
//    }
//
//    private Interview mockInterview(Long id) {
//        Interview interview = mock(Interview.class);
//        when(interview.getId()).thenReturn(id);
//        return interview;
//    }
//
//    // -------------------------------------------------------
//    // Tests
//    // -------------------------------------------------------
//
//    @Test
//    @DisplayName("질문 업데이트 성공 - 신규 + 수정 + 삭제 반영")
//    void updateQuestionsSuccess() {
//
//        Long interviewId = 1L;
//
//        Interview interview = mockInterview(interviewId);
//        User user = mockUser(1L);
//
//        when(interviewRepository.findById(interviewId))
//                .thenReturn(Optional.of(interview));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(participantRepository.existsByInterviewIdAndUserId(1L, 1L))
//                .thenReturn(true);
//
//        // 삭제하려는 질문 ID 2개 -> 실제 삭제 2개 성공
//        when(questionRepository.deleteByIdsAndInterviewId(anyList(), eq(interviewId)))
//                .thenReturn(2);
//
//        // 기존 질문 mock
//        InterviewQuestion exist = mock(InterviewQuestion.class);
//
//        // 반드시 추가해야 하는 부분
//        when(exist.getInterview()).thenReturn(interview);
//
//        when(questionRepository.findById(1001L))
//                .thenReturn(Optional.of(exist));
//
//        InterviewQuestionUpdateRequestDto request =
//                new InterviewQuestionUpdateRequestDto(
//                        List.of(
//                                new InterviewQuestionUpdateRequestDto.QuestionUpdateItem(
//                                        1001L, QuestionType.TECH, "수정된 질문입니다."
//                                ),
//                                new InterviewQuestionUpdateRequestDto.QuestionUpdateItem(
//                                        null, QuestionType.CORE, "새 질문입니다."
//                                )
//                        ),
//                        List.of(10L, 20L)
//                );
//
//        interviewQuestionService.updateQuestions(interviewId, request);
//
//        verify(questionRepository).deleteByIdsAndInterviewId(anyList(), eq(interviewId));
//        verify(questionRepository).findById(1001L);
//
//        // 수정 호출 검증
//        verify(exist).update(QuestionType.TECH, "수정된 질문입니다.");
//
//        // 새 질문 저장 검증
//        verify(questionRepository).save(any(InterviewQuestion.class));
//    }
//
//    @Test
//    @DisplayName("면접 없음 → INTERVIEW_NOT_FOUND")
//    void updateQuestionsFail_InterviewNotFound() {
//
//        when(interviewRepository.findById(1L))
//                .thenReturn(Optional.empty());
//
//        InterviewQuestionUpdateRequestDto request =
//                new InterviewQuestionUpdateRequestDto(
//                        List.of(),
//                        null
//                );
//
//        assertThatThrownBy(() -> interviewQuestionService.updateQuestions(1L, request))
//                .isInstanceOf(ApplicationException.class)
//                .hasMessage(InterviewErrorCase.INTERVIEW_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    @DisplayName("요청 형식 오류 → INVALID_FORMAT")
//    void updateQuestionsFail_InvalidFormat() {
//
//        Interview interview = mockInterview(1L);
//        User user = mockUser(1L);
//
//        when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(participantRepository.existsByInterviewIdAndUserId(1L, 1L))
//                .thenReturn(true);
//
//        // request.questions = null
//        InterviewQuestionUpdateRequestDto request =
//                new InterviewQuestionUpdateRequestDto(null, null);
//
//        assertThatThrownBy(() -> interviewQuestionService.updateQuestions(1L, request))
//                .isInstanceOf(ApplicationException.class)
//                .hasMessage(InterviewQuestionErrorCase.INVALID_FORMAT.getMessage());
//    }
//
//    @Test
//    @DisplayName("권한 없음 → FORBIDDEN")
//    void updateQuestionsFail_Forbidden() {
//
//        Interview interview = mockInterview(1L);
//        User user = mockUser(1L);
//
//        when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(participantRepository.existsByInterviewIdAndUserId(1L, 1L))
//                .thenReturn(false); // 권한 없음
//
//        InterviewQuestionUpdateRequestDto request =
//                new InterviewQuestionUpdateRequestDto(
//                        List.of(),
//                        null
//                );
//
//        assertThatThrownBy(() -> interviewQuestionService.updateQuestions(1L, request))
//                .isInstanceOf(ApplicationException.class)
//                .hasMessage(InterviewQuestionErrorCase.FORBIDDEN.getMessage());
//    }
//
//    @Test
//    @DisplayName("삭제 실패 - 일부 삭제되지 않음 → INVALID_DELETE_TARGET")
//    void updateQuestionsFail_InvalidDeleteTarget() {
//
//        Interview interview = mockInterview(1L);
//        User user = mockUser(1L);
//
//        when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(participantRepository.existsByInterviewIdAndUserId(1L, 1L))
//                .thenReturn(true);
//
//        // 요청은 3개 삭제 요청했는데 실제 삭제는 1개만
//        when(questionRepository.deleteByIdsAndInterviewId(anyList(), eq(1L)))
//                .thenReturn(1);
//
//        InterviewQuestionUpdateRequestDto request =
//                new InterviewQuestionUpdateRequestDto(
//                        List.of(),
//                        List.of(10L, 20L, 30L)
//                );
//
//        assertThatThrownBy(() -> interviewQuestionService.updateQuestions(1L, request))
//                .isInstanceOf(ApplicationException.class)
//                .hasMessage(InterviewQuestionErrorCase.INVALID_DELETE_TARGET.getMessage());
//    }
//
//    @Test
//    @DisplayName("기존 질문 찾기 실패 → INTERVIEW_QUESTION_NOT_FOUND")
//    void updateQuestionsFail_ExistQuestionNotFound() {
//
//        Interview interview = mockInterview(1L);
//        User user = mockUser(1L);
//
//        when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(participantRepository.existsByInterviewIdAndUserId(1L, 1L))
//                .thenReturn(true);
//
//        when(questionRepository.findById(1001L))
//                .thenReturn(Optional.empty());
//
//        InterviewQuestionUpdateRequestDto request =
//                new InterviewQuestionUpdateRequestDto(
//                        List.of(
//                                new InterviewQuestionUpdateRequestDto.QuestionUpdateItem(
//                                        1001L, QuestionType.TECH, "수정"
//                                )
//                        ),
//                        null
//                );
//
//        assertThatThrownBy(() -> interviewQuestionService.updateQuestions(1L, request))
//                .isInstanceOf(ApplicationException.class)
//                .hasMessage(InterviewQuestionErrorCase.INTERVIEW_QUESTION_NOT_FOUND.getMessage());
//    }
//}
