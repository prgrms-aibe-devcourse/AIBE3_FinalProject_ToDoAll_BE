package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewParticipant;
import com.server.interview.domain.InterviewStatus;
import com.server.interview.dto.*;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewQuestionRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.jd.domain.JobDescription;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.domain.Resume;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

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

        interviewService = new InterviewService(
                interviewRepository,
                participantRepository,
                noteRepository,
                questionRepository,
                jobRepo,
                resumeRepo,
                userRepo
        );
    }


    @Test
    @DisplayName("인터뷰 생성 성공")
    void createInterviewSuccess() {
        // given
        Long jdId = 1L;
        Long resumeId = 10L;
        LocalDateTime scheduledAt = LocalDateTime.of(2025, 12, 1, 10, 0);

        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                jdId, resumeId, List.of(2L, 3L), scheduledAt
        );

        JobDescription jd = mock(JobDescription.class);
        Resume resume = mock(Resume.class);
        User organizer = User.of(
                "test@test.com",
                "pw",
                "홍길동",
                "닉네임",
                "010-1111-2222",
                LocalDate.now(),
                "M",
                "Company",
                "Developer"
        );
        ReflectionTestUtils.setField(organizer, "id", 1L);

        when(jobRepo.findById(jdId)).thenReturn(Optional.of(jd));
        when(resumeRepo.findById(resumeId)).thenReturn(Optional.of(resume));
        when(userRepo.findById(1L)).thenReturn(Optional.of(organizer));
        when(interviewRepository.save(any())).thenAnswer(inv -> {
            Interview interview = inv.getArgument(0);
            ReflectionTestUtils.setField(interview, "id", 100L);
            return interview;
        });

        // when
        InterviewCreateResponseDto response = interviewService.create(request);

        // then
        assertThat(response.interviewId()).isEqualTo(100L);

        verify(interviewRepository).save(any(Interview.class));
        verify(participantRepository).save(any(InterviewParticipant.class));
        verify(participantRepository).saveAll(anyList());
    }


    @Test
    @DisplayName("JD가 존재하지 않으면 404 에러 발생")
    void createInterviewFail_JD_NotFound() {
        // given
        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                999L, 1L, List.of(2L), LocalDateTime.now()
        );

        when(jobRepo.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interviewService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(JobErrorCase.JOB_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("Resume이 존재하지 않으면 404 에러 발생")
    void createInterviewFail_Resume_NotFound() {
        // given
        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                1L, 999L, List.of(2L), LocalDateTime.now()
        );

        when(jobRepo.findById(1L)).thenReturn(Optional.of(mock(JobDescription.class)));
        when(resumeRepo.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interviewService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ResumeErrorCase.RESUME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("인터뷰 조회 성공 - 다음 페이지 존재 (hasNext = true)")
    void getInterviews_HasNext() {
        // given
        InterviewSearchCondition condition = new InterviewSearchCondition(
                1L,               // jdId
                "WAITING",        // ← 실제 enum 값으로 변경
                3,                // limit
                null,             // cursor
                "createdAt,desc"  // sort
        );

        // limit = 3 → limit+1 = 4개 반환하게 설정
        InterviewSummaryDto d1 = mockDto(10L);
        InterviewSummaryDto d2 = mockDto(9L);
        InterviewSummaryDto d3 = mockDto(8L);
        InterviewSummaryDto d4 = mockDto(7L); // hasNext detection 용

        when(interviewRepository.searchInterviews(
                eq(1L), eq("WAITING"), eq(null), eq("createdAt,desc"), eq(4)
        )).thenReturn(List.of(d1, d2, d3, d4));

        // when
        InterviewListResponseDto response = interviewService.getInterviews(condition);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(8L);  // limit=3 → 3번째 요소(id=8)
        assertThat(response.data().size()).isEqualTo(3);
    }



    @Test
    @DisplayName("인터뷰 조회 성공 - 다음 페이지 없음 (hasNext = false)")
    void getInterviews_NoNext() {
        // given
        InterviewSearchCondition condition = new InterviewSearchCondition(
                null,
                "ALL",
                3,
                null,
                "createdAt,desc"
        );

        InterviewSummaryDto d1 = mockDto(10L);
        InterviewSummaryDto d2 = mockDto(9L);

        // limit+1 = 4 요청, 실제는 2개 반환
        when(interviewRepository.searchInterviews(
                eq(null), eq(null), eq(null), eq("createdAt,desc"), eq(4)
        )).thenReturn(List.of(d1, d2));

        // when
        InterviewListResponseDto response = interviewService.getInterviews(condition);

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.data().size()).isEqualTo(2);
    }


    /**
     * 테스트용 Interview Mock 생성 도우미
     */
    private Interview mockInterview(Long id, Long jdId, String jdTitle, String resumeName) {

        // JobDescription 생성
        JobDescription jd = mock(JobDescription.class);
        ReflectionTestUtils.setField(jd, "id", jdId);
        when(jd.getId()).thenReturn(jdId);
        when(jd.getTitle()).thenReturn(jdTitle);

        // Resume 생성
        Resume resume = mock(Resume.class);
        ReflectionTestUtils.setField(resume, "id", 1L);
        when(resume.getName()).thenReturn(resumeName);

        // Organizer User
        User organizer = mock(User.class);
        ReflectionTestUtils.setField(organizer, "id", 99L);
        when(organizer.getId()).thenReturn(99L);

        // Interview 생성 (팩토리 메서드 사용)
        Interview interview = Interview.of(
                jd,
                resume,
                organizer,
                LocalDateTime.now(),
                InterviewStatus.WAITING
        );

        // ID 설정
        ReflectionTestUtils.setField(interview, "id", id);
        ReflectionTestUtils.setField(interview, "createdAt", LocalDateTime.now());

        return interview;
    }

    private InterviewSummaryDto mockDto(Long id) {
        return new InterviewSummaryDto(
                id,
                1L,
                "백엔드 개발자",
                "홍길동",
                "SCHEDULED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("인터뷰 삭제 성공")
    void deleteInterviewSuccess() {
        // given
        Long interviewId = 1L;

        // organizer
        User organizer = User.of(
                "org@test.com",
                "pw",
                "주최자",
                "organizer",
                "010-2222-3333",
                LocalDate.now(),
                "M",
                "Company",
                "Dev"
        );
        ReflectionTestUtils.setField(organizer, "id", 1L);

        // 인터뷰 Mock
        Interview mockInterview = mock(Interview.class);
        when(mockInterview.getOrganizer()).thenReturn(organizer);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(mockInterview));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(organizer));

        // when
        interviewService.deleteInterview(interviewId);

        // then
        verify(interviewRepository).delete(mockInterview);
    }

    @Test
    @DisplayName("인터뷰 삭제 실패 - 인터뷰 없음 (NOT_FOUND)")
    void deleteInterview_NotFound() {
        // given
        when(interviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interviewService.deleteInterview(999L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(InterviewErrorCase.INTERVIEW_NOT_FOUND.getMessage());

        verify(interviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("인터뷰 삭제 실패 - 권한 없음 (FORBIDDEN)")
    void deleteInterview_Forbidden() {
        // given
        Long interviewId = 10L;

        // organizer(현재 로그인 사용자)
        User requestUser = User.of(
                "org@test.com",
                "pw",
                "요청자",
                "req",
                "010-2222-3333",
                LocalDate.now(),
                "M",
                "Company",
                "Dev"
        );
        ReflectionTestUtils.setField(requestUser, "id", 1L);

        // 인터뷰의 실제 organizer = 다른 사람 (id=2)
        User realOwner = User.of(
                "real@test.com",
                "pw",
                "실제주최자",
                "real",
                "010-4444-5555",
                LocalDate.now(),
                "M",
                "Company",
                "Dev"
        );
        ReflectionTestUtils.setField(realOwner, "id", 2L);

        Interview mockInterview = mock(Interview.class);
        when(mockInterview.getOrganizer()).thenReturn(realOwner);

        when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(mockInterview));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(requestUser));

        // when & then
        assertThatThrownBy(() -> interviewService.deleteInterview(interviewId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(InterviewErrorCase.INTERVIEW_DELETE_FORBIDDEN.getMessage());

        verify(interviewRepository, never()).delete(any());
    }
}
