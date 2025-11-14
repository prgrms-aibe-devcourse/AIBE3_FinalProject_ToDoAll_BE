package com.server.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
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
}
