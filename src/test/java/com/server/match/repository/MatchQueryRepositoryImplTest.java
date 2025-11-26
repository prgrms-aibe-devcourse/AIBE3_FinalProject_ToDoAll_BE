package com.server.match.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.ai.service.AiRecommendationService;
import com.server.ai.service.KeywordExtractorService;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.MatchListResponseDto;
import com.server.match.dto.MatchSearchCondition;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeStatus;
import com.server.resume.repository.ResumeRepository;
import com.server.search.service.ResumeSearchService;
import com.server.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.server.user.domain.TestFixtures.createUser;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Import({
        MatchQueryRepositoryImplTest.QueryDslTestConfig.class,
        MatchQueryRepositoryImplTest.TestServiceMocks.class
})
class MatchQueryRepositoryImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    ResumeRepository resumeRepository;

    private JobDescription jd;
    private Resume resume1;
    private Resume resume2;

    @TestConfiguration
    static class QueryDslTestConfig {
        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }

    @TestConfiguration
    static class TestServiceMocks {
        @Bean
        public ResumeSearchService resumeSearchService() {
            return Mockito.mock(ResumeSearchService.class);
        }

        @Bean
        public KeywordExtractorService keywordExtractorService() {
            return Mockito.mock(KeywordExtractorService.class);
        }

        @Bean
        public AiRecommendationService aiRecommendationService() {
            return Mockito.mock(AiRecommendationService.class);
        }
    }

    @BeforeEach
    void setUp() {
        // 유저 생성
        User author = createUser("test@example.com", "작성자");
        em.persist(author);

        // JD 생성
        jd = jobDescriptionRepository.save(JobDescription.of(
                "백엔드 개발자",
                "개발팀",
                "정규직",
                "3년 이상",
                "대졸 이상",
                "5000",
                "Spring 기반 서비스 개발",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                JobStatus.OPEN,
                "식대 지원",
                0L,
                "서울",
                null,
                author
        ));

        // Resume 생성
        resume1 = resumeRepository.save(Resume.of(
                jd, "승인", "남", LocalDate.of(1998, 4, 4),
                "test1@test.com", "010-1234-5678", "서울", "어딘가", null, null, ResumeStatus.NEW
        ));
        resume2 = resumeRepository.save(Resume.of(
                jd, "유진", "여", LocalDate.of(1995, 5, 5),
                "test2@test.com", "010-2222-3333", "부산", "어딘가", null, null, ResumeStatus.NEW
        ));

        // Match 생성
        Match m1 = Match.of(jd, resume1, LocalDateTime.now(), 80f, null, null, MatchStatus.APPLIED);
        Match m2 = Match.of(jd, resume2, LocalDateTime.now().minusDays(1), 60f, null, null, MatchStatus.APPLIED);

        matchRepository.save(m1);
        matchRepository.save(m2);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("QueryDSL: JD에 따른 이력서 목록 조회 (기본: 최신순 정렬)")
    void testSearchMatches_defaultSort() {
        MatchSearchCondition condition = new MatchSearchCondition(jd.getId(), null);
        Pageable pageable = PageRequest.of(0, 20);

        List<MatchListResponseDto> result =
                matchRepository.searchMatches(condition, pageable).getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).resumeName()).isEqualTo("승인");  // 최신 지원자
        assertThat(result.get(1).resumeName()).isEqualTo("유진");
    }

    @Test
    @DisplayName("QueryDSL: 점수순 정렬 정상 동작")
    void testSearchMatches_scoreSort() {
        MatchSearchCondition condition = new MatchSearchCondition(jd.getId(), null);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "matchScore"));

        List<MatchListResponseDto> result =
                matchRepository.searchMatches(condition, pageable).getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).matchScore()).isEqualTo(80f);
        assertThat(result.get(1).matchScore()).isEqualTo(60f);
    }

    @Test
    @DisplayName("QueryDSL: 상태 필터링 정상 동작")
    void testSearchMatches_withStatusFilter() {
        MatchSearchCondition condition = new MatchSearchCondition(jd.getId(), MatchStatus.APPLIED);
        Pageable pageable = PageRequest.of(0, 20);

        List<MatchListResponseDto> result =
                matchRepository.searchMatches(condition, pageable).getContent();

        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(r -> r.status() == MatchStatus.APPLIED)).isTrue();
    }
}