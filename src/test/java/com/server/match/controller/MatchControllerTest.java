package com.server.match.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.MatchListResponseDto;
import com.server.match.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(MatchControllerTest.MockConfig.class)
@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchService matchService;

    @Autowired
    private ObjectMapper objectMapper;


    @TestConfiguration
    static class MockConfig {
        @Bean
        public MatchService matchService() {
            return Mockito.mock(MatchService.class);
        }
    }

    @Test
    @WithMockUser
    @DisplayName("JD ID로 전체 매칭 이력서 조회 성공")
    void getMatchedResumes_success() throws Exception {
        MatchListResponseDto responseDto = MatchListResponseDto.builder()
                .resumeId(1L)
                .name("홍길동")
                .matchScore(88.5f)
                .status(MatchStatus.APPLIED)
                .skillMatchRate("75%")
                .missingSkills(List.of("Kafka", "Redis"))
                .summary("React/Node.js 기반 3년 경력 보유")
                .build();

        PageImpl<MatchListResponseDto> page = new PageImpl<>(
                List.of(responseDto),
                PageRequest.of(0, 20),
                1
        );

        Mockito.when(matchService.getMatchedResumesPaged(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/matches")
                        .param("jdId", "1")
                        .param("matchSort", "LATEST")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].resumeId").value(1L))
                .andExpect(jsonPath("$.data.content[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data.content[0].skillMatchRate").value("75%"))
                .andExpect(jsonPath("$.data.content[0].summary").value("React/Node.js 기반 3년 경력 보유"))
                .andExpect(jsonPath("$.data.content[0].missingSkills[0]").value("Kafka"));
    }

    @Test
    @WithMockUser
    @DisplayName("잘못된 JD ID로 요청 시 400 에러 반환")
    void getMatchedResumes_invalidJdId() throws Exception {
        mockMvc.perform(get("/api/v1/matches")
                        .param("jdId", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 JD ID입니다."));
    }
}

