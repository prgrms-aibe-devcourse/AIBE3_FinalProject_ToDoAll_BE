package com.server.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.dto.InterviewListResponseDto;
import com.server.interview.dto.InterviewSummaryDto;
import com.server.interview.service.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class InterviewControllerTest {

    private MockMvc mockMvc;
    private InterviewService interviewService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        interviewService = Mockito.mock(InterviewService.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        InterviewController controller = new InterviewController(interviewService);

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("인터뷰 생성 성공")
    void createInterviewSuccess() throws Exception {

        // ---------------------- REQUEST ----------------------
        InterviewCreateRequestDto request = new InterviewCreateRequestDto(
                1L,                  // JD ID
                1L,                  // Resume ID
                List.of(2L, 3L),     // Participants
                LocalDateTime.of(2025, 12, 1, 10, 0)
        );

        // ---------------------- SERVICE MOCKING ----------------------
        InterviewCreateResponseDto response =
                new InterviewCreateResponseDto(1L);

        when(interviewService.create(any(InterviewCreateRequestDto.class)))
                .thenReturn(response);

        // ---------------------- API 호출 ----------------------
        mockMvc.perform(post("/api/v1/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.interviewId").value(1L));

        // ---------------------- 검증 ----------------------
        verify(interviewService).create(any(InterviewCreateRequestDto.class));
    }

    @Test
    @DisplayName("인터뷰 조회 성공")
    void getInterviewSuccess() throws Exception {

        // ---------------------- REQUEST PARAMS ----------------------
        Long jdId = 1L;
        String status = "ALL";
        Integer limit = 6;
        Long cursor = null;
        String sort = "createdAt,desc";

        // ---------------------- SERVICE MOCK RESPONSE ----------------------
        InterviewSummaryDto summary1 = new InterviewSummaryDto(
                100L,
                1L,
                "백엔드 개발자 채용",
                "김지원",
                "SCHEDULED",
                LocalDateTime.of(2025, 12, 1, 10, 0),
                LocalDateTime.of(2025, 11, 15, 12, 0)
        );

        InterviewSummaryDto summary2 = new InterviewSummaryDto(
                99L,
                1L,
                "백엔드 개발자 채용",
                "박민수",
                "WAITING",
                LocalDateTime.of(2025, 12, 2, 11, 0),
                LocalDateTime.of(2025, 11, 14, 11, 0)
        );

        InterviewListResponseDto response = new InterviewListResponseDto(
                List.of(summary1, summary2),
                99L,       // nextCursor
                true       // hasNext
        );

        when(interviewService.getInterviews(any())).thenReturn(response);

        // ---------------------- API 호출 ----------------------
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/v1/interviews")
                                .param("jdId", jdId.toString())
                                .param("status", status)
                                .param("limit", limit.toString())
                                .param("sort", sort)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.data[0].interviewId").value(100L))
                .andExpect(jsonPath("$.data.data[0].candidateName").value("김지원"))
                .andExpect(jsonPath("$.data.nextCursor").value(99L))
                .andExpect(jsonPath("$.data.hasNext").value(true));

        // ---------------------- 검증 ----------------------
        verify(interviewService).getInterviews(any());
    }

}
