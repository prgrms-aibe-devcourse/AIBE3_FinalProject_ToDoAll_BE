package com.server.interview.service;

import com.server.global.auth.AuthUtils;
import com.server.interview.domain.Interview;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.Gender;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    @InjectMocks
    private InterviewService interviewService;

    @Mock
    private JobDescriptionRepository jobDescriptionRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewParticipantRepository interviewParticipantRepository;
    @Mock
    private InterviewNoteRepository interviewNoteRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private MockedStatic<AuthUtils> authUtilsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authUtilsMock = mockStatic(AuthUtils.class);
        authUtilsMock.when(AuthUtils::getCurrentUserId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        authUtilsMock.close();
    }

    @Test
    @DisplayName("면접 생성 성공 테스트")
    void createInterview_success() {
        // given
        Long jdId = 10L;
        Long resumeId = 20L;

        JobDescription jd = mock(JobDescription.class);
        Resume resume = mock(Resume.class);

        // organizer 사용자 생성
        User organizer = User.of(
                "organizer@test.com",
                "encodedPassword",
                "조직자",
                "닉네임",
                "01012345678",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                "Company",
                "Developer"
        );
        ReflectionTestUtils.setField(organizer, "id", 1L);

        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(jd));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));

        when(interviewRepository.save(any(Interview.class))).thenAnswer(
                (Answer<Interview>) invocation -> {
                    Interview saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 999L); // persist ID mock
                    return saved;
                }
        );

        // Observer 사용자 생성
        User observer = User.of(
                "observer@test.com",
                "encodedPw",
                "옵저버",
                "옵니",
                "01099999999",
                LocalDate.of(1999, 5, 10),
                Gender.FEMALE,
                "Company2",
                "Manager"
        );
        ReflectionTestUtils.setField(observer, "id", 2L);

        when(userRepository.findAllById(anySet()))
                .thenReturn(List.of(observer));

        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                jdId,
                resumeId,
                List.of(1L, 2L, 2L),
                LocalDateTime.now()
        );

        // when
        InterviewCreateResponseDto response = interviewService.create(request);

        // then
        assertThat(response.interviewId()).isEqualTo(999L);

        verify(interviewRepository).save(any());
        verify(interviewParticipantRepository, times(1)).save(any()); // organizer 1명
        verify(interviewParticipantRepository, times(1)).saveAll(any()); // observer 저장
        verify(interviewNoteRepository, times(1)).save(any());
    }
}
