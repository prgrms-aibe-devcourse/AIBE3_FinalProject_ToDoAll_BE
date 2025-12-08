package com.server.interview.service;

import com.server.global.auth.AuthUtils;
import com.server.interview.domain.Interview;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeStatus;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.Gender;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class InterviewServiceTest {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private InterviewParticipantRepository interviewParticipantRepository;

    @Autowired
    private InterviewNoteRepository interviewNoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private ResumeRepository resumeRepository;
//
//    @MockBean
//    private ApplicationEventPublisher applicationEventPublisher; // 이벤트 Mock

    private MockedStatic<AuthUtils> authMock;
    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = User.of(
                "organizer@test.com", "pw",
                "Organ", "izer",
                "01012341234",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "Company",
                "Developer"
        );
        userRepository.save(organizer);

        authMock = mockStatic(AuthUtils.class);
        authMock.when(AuthUtils::getCurrentUserId).thenReturn(organizer.getId());
    }

    @AfterEach
    void tearDown() {
        authMock.close();
    }

    @Test
    @DisplayName("면접 생성 성공 - Interview/Participant/Note 모두 저장")
    void createInterview_success() {
        // given
        User user1 = User.of("user1@test.com", "pw", "User", "One", null, null, null, null, null);
        User user2 = User.of("user2@test.com", "pw", "User", "Two", null, null, null, null, null);
        userRepository.saveAll(List.of(user1, user2));

        JobDescription jd = JobDescription.of(
                "백엔드 개발자", null, null, null, null, null,
                null, null, null, JobStatus.OPEN,
                null, 0L, null, null, organizer
        );
        jobDescriptionRepository.save(jd);

        Resume resume = Resume.of(
                jd,
                organizer.getName(),
                "M",
                LocalDate.of(1990, 1, 1),
                organizer.getEmail(),
                "010-0000-1111",
                "Seoul",
                "101-202",
                null, null,
                ResumeStatus.NEW
        );
        resumeRepository.save(resume);

        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                jd.getId(),
                resume.getId(),
                List.of(user1.getId(), user2.getId(), organizer.getId()),
                LocalDateTime.now().plusDays(1)
        );

        // when
        InterviewCreateResponseDto response = interviewService.create(request);

        // then
        Interview interview = interviewRepository.findById(response.interviewId())
                .orElseThrow();

        assertThat(interview.getOrganizer().getId()).isEqualTo(organizer.getId());
        assertThat(interviewParticipantRepository.findUserIdsByInterviewId(interview.getId()))
                .hasSize(3); // organizer + 2 observers
        assertThat(interviewNoteRepository.findByInterviewId(interview.getId()))
                .isNotNull();
    }
}

