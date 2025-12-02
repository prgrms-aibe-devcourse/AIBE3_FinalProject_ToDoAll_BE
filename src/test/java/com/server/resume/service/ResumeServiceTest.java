package com.server.resume.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.Skill;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.SkillRepository;
import com.server.match.domain.Match;
import com.server.match.repository.MatchRepository;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeStatus;
import com.server.resume.dto.*;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.search.repository.ResumeSearchRepository;
import com.server.search.service.ResumeSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobDescriptionRepository jobDescriptionRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ResumeSearchRepository resumeSearchRepository;

    @Mock
    private ResumeSearchService resumeSearchService;

    @InjectMocks
    private ResumeService resumeService;

    @Mock
    private MatchRepository matchRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getResumeById - 성공")
    void getResumeById_success() {
        Resume resume = mock(Resume.class);

        when(resumeRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(resume));

        resumeService.getResumeById(1L);

        verify(resumeRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("getResumeById - 실패(찾을 수 없음)")
    void getResumeById_fail() {
        when(resumeRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.getResumeById(1L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(ResumeErrorCase.RESUME_NOT_FOUND);
    }

    @Test
    @DisplayName("createResume - 성공")
    void createResume_success() {
        JobDescription jd = mock(JobDescription.class);
        when(jd.getId()).thenReturn(10L);

        when(jobDescriptionRepository.findById(10L))
                .thenReturn(Optional.of(jd));

        Resume savedResume = mock(Resume.class);
        when(savedResume.getId()).thenReturn(100L);

        when(resumeRepository.save(any())).thenReturn(savedResume);

        when(matchRepository.existsByJobDescription_IdAndResume_Id(10L, 100L)).thenReturn(false);

        when(matchRepository.save(any())).thenReturn(null);

        when(resumeSearchRepository.count()).thenReturn(0L);

        ResumeCreateRequestDto request = new ResumeCreateRequestDto(
                "홍길동",
                10L,
                "M",
                LocalDate.of(1990, 1, 1),
                "test@test.com",
                "01012345678",
                "서울",
                "강남",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                "resume-url",
                "portfolio-url"
        );

        resumeService.createResume(request);

        verify(jobDescriptionRepository).findById(10L);
        verify(resumeRepository).save(any(Resume.class));
        verify(matchRepository).existsByJobDescription_IdAndResume_Id(10L, 100L);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("createResume - 실패(JD null)")
    void createResume_jdNull_fail() {
        ResumeCreateRequestDto request = new ResumeCreateRequestDto(
                "홍길동",
                null, // JD null
                "M",
                LocalDate.of(1990, 1, 1),
                "test@test.com",
                "01012345678",
                "서울",
                "강남",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                "resume-url",
                "portfolio-url"
        );

        assertThatThrownBy(() -> resumeService.createResume(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(ResumeErrorCase.JD_NOT_FOUND);
    }

    @Test
    @DisplayName("createResume - 실패(JD를 DB에서 못 찾음)")
    void createResume_jdNotFound_fail() {
        when(jobDescriptionRepository.findById(10L))
                .thenReturn(Optional.empty());

        ResumeCreateRequestDto request = new ResumeCreateRequestDto(
                "홍길동",
                10L,
                "M",
                LocalDate.of(1990, 1, 1),
                "test@test.com",
                "01012345678",
                "서울",
                "강남",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                "resume-url",
                "portfolio-url"
        );

        assertThatThrownBy(() -> resumeService.createResume(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(ResumeErrorCase.JD_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteResume - 성공")
    void deleteResume_success() {
        Resume resume = mock(Resume.class);

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L);

        verify(resumeRepository).delete(resume);
    }

    @Test
    @DisplayName("deleteResume - 실패(없음)")
    void deleteResume_fail() {
        when(resumeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.deleteResume(1L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(ResumeErrorCase.RESUME_NOT_FOUND);
    }

    @Test
    @DisplayName("updateResumeStatus - 성공")
    void updateResumeStatus_success() {
        Resume resume = mock(Resume.class);

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        ResumeStatusUpdateDto dto = new ResumeStatusUpdateDto(ResumeStatus.BOOKMARK);

        resumeService.updateResumeStatus(1L, dto);

        verify(resume).updateStatus(ResumeStatus.BOOKMARK);
    }

    @Test
    @DisplayName("updateResumeStatus - 실패(이력서 없음)")
    void updateResumeStatus_resumeNotFound_fail() {
        when(resumeRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResumeStatusUpdateDto dto = new ResumeStatusUpdateDto(ResumeStatus.BOOKMARK);

        assertThatThrownBy(() -> resumeService.updateResumeStatus(1L, dto))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(ResumeErrorCase.RESUME_NOT_FOUND);
    }

    @Test
    @DisplayName("getOrCreateSkill - 기존 스킬 존재")
    void getOrCreateSkill_exist() {
        Skill skill = Skill.of("Java");

        when(skillRepository.findByName("Java"))
                .thenReturn(Optional.of(skill));

        Skill result = resumeService.getOrCreateSkill("Java");

        assertThat(result).isEqualTo(skill);
    }

    @Test
    @DisplayName("getOrCreateSkill - 기존 스킬 없음 → 새로 생성")
    void getOrCreateSkill_create() {
        Skill saved = Skill.of("Spring");

        when(skillRepository.findByName("Spring"))
                .thenReturn(Optional.empty());

        when(skillRepository.save(any()))
                .thenReturn(saved);

        Skill result = resumeService.getOrCreateSkill("Spring");

        assertThat(result).isEqualTo(saved);
    }
}
