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
import com.server.match.domain.Match;
import com.server.match.repository.MatchRepository;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.Gender;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    @InjectMocks
    private InterviewService interviewService;

    @Mock private JobDescriptionRepository jobDescriptionRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private UserRepository userRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private InterviewParticipantRepository interviewParticipantRepository;
    @Mock private InterviewNoteRepository interviewNoteRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private MatchRepository matchRepository;

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
        Match match = mock(Match.class);

        // mock 객체 getId() 스텁 (ReflectionTestUtils는 mock에서 작동 안 함!)
        when(jd.getId()).thenReturn(jdId);
        when(resume.getId()).thenReturn(resumeId);

        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(jd));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        // organizer 설정
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));

        // match 는 anyLong() 으로 처리하는 것이 가장 안전
        when(matchRepository.findByJobDescription_IdAndResume_Id(anyLong(), anyLong()))
                .thenReturn(Optional.of(match));

        // Interview 저장 시 id 설정
        when(interviewRepository.save(any(Interview.class)))
                .thenAnswer(invocation -> {
                    Interview saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 999L);
                    return saved;
                });

        // Observer 생성
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

        InterviewCreateRequestDto request =
                new InterviewCreateRequestDto(
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
        verify(interviewParticipantRepository, times(1)).save(any()); // organizer
        verify(interviewParticipantRepository, times(1)).saveAll(any()); // observers
        verify(interviewNoteRepository, times(1)).save(any());

        // Match 상태 업데이트 호출 검증
        verify(match).updateStatus(any());
    }
}
