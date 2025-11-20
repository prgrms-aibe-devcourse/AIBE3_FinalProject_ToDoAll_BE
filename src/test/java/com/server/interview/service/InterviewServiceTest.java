package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewNote;
import com.server.interview.dto.*;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.*;
import com.server.jd.domain.JobDescription;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.domain.Resume;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.Gender;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    private InterviewRepository interviewRepository;
    private InterviewParticipantRepository participantRepository;
    private InterviewNoteRepository noteRepository;
    private InterviewQuestionRepository questionRepository;
    private JobDescriptionRepository jobRepo;
    private ResumeRepository resumeRepo;
    private UserRepository userRepo;
    private InterviewEvaluationRepository  interviewEvaluationRepository;
    private ApplicationEventPublisher eventPublisher;

    private InterviewService interviewService;

    @BeforeEach
    void setUp() {
        interviewRepository = Mockito.mock(InterviewRepository.class);
        participantRepository = Mockito.mock(InterviewParticipantRepository.class);
        noteRepository = Mockito.mock(InterviewNoteRepository.class);
        questionRepository = Mockito.mock(InterviewQuestionRepository.class);
        jobRepo = Mockito.mock(JobDescriptionRepository.class);
        resumeRepo = Mockito.mock(ResumeRepository.class);
        userRepo = Mockito.mock(UserRepository.class);
        interviewEvaluationRepository =  Mockito.mock(InterviewEvaluationRepository.class);
        eventPublisher =  Mockito.mock(ApplicationEventPublisher.class);

        interviewService = new InterviewService(
                interviewRepository,
                participantRepository,
                noteRepository,
                questionRepository,
                jobRepo,
                resumeRepo,
                userRepo,
                interviewEvaluationRepository,
                eventPublisher
        );
    }

    // ======================= 공통 User 생성 메서드 =========================

    private User createUser(Long id, String email, String name) {
        User user = User.of(
                email,
                "pw",
                name,
                "nickname",
                "010-1111-2222",
                LocalDate.now(),
                Gender.M,
                "Company",
                "Dev"
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // ============================= CREATE 테스트 ==============================

    @Test
    @DisplayName("인터뷰 생성 성공")
    void createInterviewSuccess() {
        Long jdId = 1L;
        Long resumeId = 10L;

        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                jdId,
                resumeId,
                List.of(2L, 3L),
                LocalDateTime.of(2025, 12, 1, 10, 0)
        );

        JobDescription jd = mock(JobDescription.class);
        Resume resume = mock(Resume.class);
        User organizer = createUser(1L, "test@test.com", "홍길동");

        when(jobRepo.findById(jdId)).thenReturn(Optional.of(jd));
        when(resumeRepo.findById(resumeId)).thenReturn(Optional.of(resume));
        when(userRepo.findById(1L)).thenReturn(Optional.of(organizer));

        when(interviewRepository.save(any())).thenAnswer(invocation -> {
            Interview interview = invocation.getArgument(0);
            ReflectionTestUtils.setField(interview, "id", 100L);
            return interview;
        });

        // when
        InterviewCreateResponseDto response = interviewService.create(request);

        // then
        assertThat(response.interviewId()).isEqualTo(100L);
        verify(interviewRepository).save(any(Interview.class));
        verify(participantRepository).save(any());
        verify(participantRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("JD가 존재하지 않으면 404 에러 발생")
    void createInterviewFail_JD_NotFound() {
        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                999L, 1L, List.of(2L), LocalDateTime.now()
        );
        when(jobRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(JobErrorCase.JOB_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Resume이 존재하지 않으면 404 에러 발생")
    void createInterviewFail_Resume_NotFound() {
        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                1L, 999L, List.of(2L), LocalDateTime.now()
        );

        when(jobRepo.findById(1L)).thenReturn(Optional.of(mock(JobDescription.class)));
        when(resumeRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ResumeErrorCase.RESUME_NOT_FOUND.getMessage());
    }

    // ============================= 조회 테스트 ==============================

    @Test
    @DisplayName("인터뷰 조회 성공 - 다음 페이지 존재 (hasNext = true)")
    void getInterviews_HasNext() {
        InterviewSearchConditionDto condition = new InterviewSearchConditionDto(
                1L,
                "WAITING",
                3,
                null,
                "createdAt,desc"
        );

        InterviewSummaryDto d1 = mockDto(10L);
        InterviewSummaryDto d2 = mockDto(9L);
        InterviewSummaryDto d3 = mockDto(8L);
        InterviewSummaryDto d4 = mockDto(7L);

        when(interviewRepository.searchInterviews(
                eq(1L), eq("WAITING"), eq(null), eq("createdAt,desc"), eq(4)
        )).thenReturn(List.of(d1, d2, d3, d4));

        InterviewListResponseDto response = interviewService.getInterviews(condition);

        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(8L);
        assertThat(response.data().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("인터뷰 조회 성공 - 다음 페이지 없음 (hasNext = false)")
    void getInterviews_NoNext() {
        InterviewSearchConditionDto condition = new InterviewSearchConditionDto(
                null,
                "ALL",
                3,
                null,
                "createdAt,desc"
        );

        InterviewSummaryDto d1 = mockDto(10L);
        InterviewSummaryDto d2 = mockDto(9L);

        when(interviewRepository.searchInterviews(
                eq(null), eq(null), eq(null), eq("createdAt,desc"), eq(4)
        )).thenReturn(List.of(d1, d2));

        InterviewListResponseDto response = interviewService.getInterviews(condition);

        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.data().size()).isEqualTo(2);
    }

    // ============================= 삭제 테스트 ==============================

    @Test
    @DisplayName("인터뷰 삭제 성공")
    void deleteInterviewSuccess() {
        Long interviewId = 1L;

        // 주최자
        User organizer = createUser(1L, "org@test.com", "주최자");

        // 인터뷰 Mock
        Interview mockInterview = mock(Interview.class);
        when(mockInterview.getOrganizer()).thenReturn(organizer);

        // 인터뷰 조회
        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(mockInterview));

        // 유저 조회
        when(userRepo.findById(1L))
                .thenReturn(Optional.of(organizer));

        // 인터뷰 노트 Mock
        InterviewNote mockNote = mock(InterviewNote.class);
        when(noteRepository.findByInterviewId(interviewId))
                .thenReturn(Optional.of(mockNote));

        // 테스트 실행
        interviewService.deleteInterview(interviewId);

        // 인터뷰 노트 삭제
        verify(noteRepository).delete(mockNote);

        // 질문 삭제
        verify(questionRepository).deleteByInterviewId(interviewId);

        // 평가 삭제
        verify(interviewEvaluationRepository).deleteByInterviewId(interviewId);

        // 인터뷰 삭제
        verify(interviewRepository).delete(mockInterview);
    }


    @Test
    @DisplayName("인터뷰 삭제 실패 - 인터뷰 없음 (NOT_FOUND)")
    void deleteInterview_NotFound() {
        when(interviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.deleteInterview(999L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(InterviewErrorCase.INTERVIEW_NOT_FOUND.getMessage());

        verify(interviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("인터뷰 삭제 실패 - 권한 없음 (FORBIDDEN)")
    void deleteInterview_Forbidden() {
        Long interviewId = 10L;

        User requestUser = createUser(1L, "req@test.com", "요청자");
        User realOwner = createUser(2L, "real@test.com", "진짜주최자");

        Interview mockInterview = mock(Interview.class);
        when(mockInterview.getOrganizer()).thenReturn(realOwner);

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(mockInterview));
        when(userRepo.findById(1L)).thenReturn(Optional.of(requestUser));

        assertThatThrownBy(() -> interviewService.deleteInterview(interviewId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(InterviewErrorCase.INTERVIEW_DELETE_FORBIDDEN.getMessage());

        verify(interviewRepository, never()).delete(any());
    }

    // ============================= DTO Mock ==============================

    private InterviewSummaryDto mockDto(Long id) {
        return new InterviewSummaryDto(
                id,
                1L,
                "백엔드 개발자",
                "홍길동",
                "WAITING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

