package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewParticipant;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.repository.InterviewParticipantRepository;
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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    private InterviewRepository interviewRepository;
    private InterviewParticipantRepository participantRepository;
    private JobDescriptionRepository jobRepo;
    private ResumeRepository resumeRepo;
    private UserRepository userRepo;

    private InterviewService interviewService;

    @BeforeEach
    void setUp() {
        interviewRepository = Mockito.mock(InterviewRepository.class);
        participantRepository = Mockito.mock(InterviewParticipantRepository.class);
        jobRepo = Mockito.mock(JobDescriptionRepository.class);
        resumeRepo = Mockito.mock(ResumeRepository.class);
        userRepo = Mockito.mock(UserRepository.class);

        interviewService = new InterviewService(
                interviewRepository,
                participantRepository,
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
}
