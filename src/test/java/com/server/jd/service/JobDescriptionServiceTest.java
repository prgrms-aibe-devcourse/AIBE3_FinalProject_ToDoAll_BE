package com.server.jd.service;

import com.server.global.auth.AuthUtils;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.*;
import com.server.jd.dto.*;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.*;
import com.server.jd.repository.projection.SkillByJobProjection;
import com.server.user.domain.Gender;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobDescriptionServiceTest {

    @InjectMocks
    private JobDescriptionService jobDescriptionService;

    @Mock
    private JobDescriptionRepository jobRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private JobRequiredSkillRepository jobRequiredSkillRepository;
    @Mock
    private JobPreferredSkillRepository jobPreferredSkillRepository;

    private User testAuthor;
    private JobDescription testJob;


    private <T> T setId(T target, Long id) {
        try {
            Field idField = target.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패: " + e.getMessage());
        }
        return target;
    }

    @BeforeEach
    void setUp() {
        testAuthor = User.of(
                "test@test.com",
                "encodedPassword",
                "Author",
                "AuthorNick",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "TestCompany",
                "Developer"
        );
        setId(testAuthor, 1L);

        testJob = JobDescription.of(
                "테스트 JD", "개발팀", "정규직", "주니어", "학사", "5000",
                "상세 설명", null, LocalDate.now().plusDays(30),
                JobStatus.OPEN, "복지", 0L, "서울", "thumbnail.url", testAuthor
        );
        setId(testJob, 10L);
    }

    @Test
    @DisplayName("JD 초안 생성 성공 - 기존 스킬/새 스킬 포함")
    void createDraft_Success() {
        JobDescriptionCreateRequestDto request = new JobDescriptionCreateRequestDto(
                "New JD", "Dev", "Full", "Jr", "BS", "50M", "Desc",
                LocalDate.now().plusDays(7), "Benefit", "Seoul", "url",
                1L, List.of("Java", "Spring"), List.of("Docker")
        );

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testAuthor));

            Skill javaSkill = setId(Skill.of("java"), 1L);
            Skill springSkill = setId(Skill.of("spring"), 2L);
            Skill dockerSkill = setId(Skill.of("docker"), 3L);

            when(skillRepository.findByName("java")).thenReturn(Optional.of(javaSkill));
            when(skillRepository.findByName("spring")).thenReturn(Optional.empty());
            when(skillRepository.findByName("docker")).thenReturn(Optional.empty());

            when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
                Skill skillToSave = invocation.getArgument(0);
                if (skillToSave.getName().equals("spring")) return springSkill;
                if (skillToSave.getName().equals("docker")) return dockerSkill;
                return skillToSave;
            });

            when(jobRepository.save(any(JobDescription.class))).thenAnswer(invocation -> {
                JobDescription jd = invocation.getArgument(0);
                setId(jd, testJob.getId());
                return jd;
            });

            doNothing().when(jobRepository).flush();


            Long resultId = jobDescriptionService.createDraft(request);

            assertThat(resultId).isEqualTo(testJob.getId());
            verify(jobRepository, times(1)).save(any(JobDescription.class));
            verify(jobRepository, times(1)).flush();
            verify(jobRequiredSkillRepository, times(1)).saveAll(anyList());
            verify(jobPreferredSkillRepository, times(1)).saveAll(anyList());
            verify(skillRepository, times(2)).save(any(Skill.class));
        }
    }
    @Test
    @DisplayName("JD 초안 생성 실패 - 작성자 User를 찾을 수 없음")
    void createDraft_UserNotFound_Failure() {
        JobDescriptionCreateRequestDto request = new JobDescriptionCreateRequestDto(
                "New JD", "Dev", "Full", "Jr", "BS", "50M", "Desc",
                LocalDate.now().plusDays(7), "Benefit", "Seoul", "url",
                1L, List.of(), List.of()
        );

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(99L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                jobDescriptionService.createDraft(request);
            });
            assertThat(exception.getErrorCase()).isEqualTo(JobErrorCase.AUTHOR_NOT_FOUND);
        }
    }



    @Test
    @DisplayName("JD 상세 조회 성공")
    void getDetail_Success() {
        Long jdId = testJob.getId();
        List<String> requiredSkills = List.of("Java", "JPA");
        List<String> preferredSkills = List.of("AWS", "MSA");

        when(jobRepository.findById(jdId)).thenReturn(Optional.of(testJob));
        when(jobRequiredSkillRepository.findRequiredSkillNamesByJobId(jdId)).thenReturn(requiredSkills);
        when(jobPreferredSkillRepository.findPreferredSkillNamesByJobId(jdId)).thenReturn(preferredSkills);

        JobDescriptionDetailResponseDto result = jobDescriptionService.getDetail(jdId);

        assertThat(result.id()).isEqualTo(jdId);
        assertThat(result.title()).isEqualTo(testJob.getTitle());
        assertThat(result.skills()).containsExactlyInAnyOrder("Java", "JPA");
        assertThat(result.preferredSkills()).containsExactlyInAnyOrder("AWS", "MSA");

        verify(jobRepository, times(1)).findById(jdId);
    }

    @Test
    @DisplayName("JD 상세 조회 실패 - JD를 찾을 수 없음")
    void getDetail_NotFound_Failure() {
        Long jdId = 99L;
        when(jobRepository.findById(jdId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            jobDescriptionService.getDetail(jdId);
        });
        assertThat(exception.getErrorCase()).isEqualTo(JobErrorCase.JOB_NOT_FOUND);
    }


    @Test
    @DisplayName("JD 상태 업데이트 성공")
    void updateStatus_Success() {
        Long jdId = testJob.getId();
        JobDescriptionStatusRequestDto request = new JobDescriptionStatusRequestDto(JobStatus.CLOSED);

        when(jobRepository.findById(jdId)).thenReturn(Optional.of(testJob));

        JobDescriptionStatusResponseDto result = jobDescriptionService.updateStatus(jdId, request);

        assertThat(result.id()).isEqualTo(jdId);
        assertThat(result.status()).isEqualTo(JobStatus.CLOSED);

        assertThat(testJob.getStatus()).isEqualTo(JobStatus.CLOSED);
    }


    @Test
    @DisplayName("JD 내용 및 스킬 업데이트 성공")
    void update_Success() {
        Long jdId = testJob.getId();
        JobDescriptionUpdateRequestDto request = new JobDescriptionUpdateRequestDto(
                "Updated Title", "New Dev Dept", "Contract", "Senior",
                "Master", "80M", "Updated Desc", LocalDate.now().plusDays(10),
                "New Benefits", "Busan", "new.url",
                List.of("Python", "Django"), List.of("Redis")
        );

        Skill pythonSkill = setId(Skill.of("python"), 10L);
        Skill djangoSkill = setId(Skill.of("django"), 11L);
        Skill redisSkill = setId(Skill.of("redis"), 12L);

        when(jobRepository.findById(jdId)).thenReturn(Optional.of(testJob));

        when(skillRepository.findByNameIn(List.of("Python", "Django")))
                .thenReturn(List.of(pythonSkill, djangoSkill));
        when(skillRepository.findByNameIn(List.of("Redis")))
                .thenReturn(List.of(redisSkill));

        when(jobRequiredSkillRepository.findRequiredSkillNamesByJobId(jdId)).thenReturn(List.of("Python", "Django"));
        when(jobPreferredSkillRepository.findPreferredSkillNamesByJobId(jdId)).thenReturn(List.of("Redis"));

        JobDescriptionDetailResponseDto result = jobDescriptionService.update(jdId, request);

        assertThat(result.id()).isEqualTo(jdId);
        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(testJob.getTitle()).isEqualTo("Updated Title");

        verify(jobRequiredSkillRepository, times(1)).deleteByJobId(jdId);
        verify(jobPreferredSkillRepository, times(1)).deleteByJobId(jdId);
        verify(jobRequiredSkillRepository, times(1)).saveAll(anyList());
        verify(jobPreferredSkillRepository, times(1)).saveAll(anyList());

        assertThat(result.skills()).containsExactlyInAnyOrder("Python", "Django");
        assertThat(result.preferredSkills()).containsExactly("Redis");
    }


    @Test
    @DisplayName("내 JD 목록 조회 성공 - 스킬 제한 포함")
    void getMyList_Success() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<JobDescription> jobList = List.of(testJob);
        Page<JobDescription> page = new PageImpl<>(jobList, pageable, 1);

        List<SkillByJobProjection> projectionList = List.of(
                new MockSkillProjection(10L, "Java"),
                new MockSkillProjection(10L, "Spring"),
                new MockSkillProjection(10L, "Docker")
        );

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(userId);
            when(jobRepository.findAllByAuthorId(userId, pageable)).thenReturn(page);
            when(jobRequiredSkillRepository.findRequiredSkillsByJobIds(List.of(10L))).thenReturn(projectionList);

            Page<JobDescriptionListResponseDto> resultPage = jobDescriptionService.getMyList(pageable, 2);

            assertThat(resultPage.getTotalElements()).isEqualTo(1);
            JobDescriptionListResponseDto result = resultPage.getContent().get(0);

            assertThat(result.requiredSkills()).hasSize(2);
            assertThat(result.requiredSkills()).contains("Java", "Spring");
            assertThat(result.requiredSkills()).doesNotContain("Docker");
        }
    }

    private record MockSkillProjection(Long jobId, String skillName) implements SkillByJobProjection {
        @Override
        public Long getJobId() {
            return jobId;
        }

        @Override
        public String getSkillName() {
            return skillName;
        }
    }
}