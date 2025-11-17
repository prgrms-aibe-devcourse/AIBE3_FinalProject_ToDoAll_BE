package com.server.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.interview.domain.QuestionType;
import com.server.interview.dto.InterviewQuestionUpdateRequestDto;
import com.server.interview.service.InterviewQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class InterviewQuestionControllerTest {

    private MockMvc mockMvc;
    private InterviewQuestionService interviewQuestionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        interviewQuestionService = Mockito.mock(InterviewQuestionService.class);
        objectMapper = new ObjectMapper();

        InterviewQuestionController controller =
                new InterviewQuestionController(interviewQuestionService);

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("면접 질문 업데이트 성공")
    void updateQuestionsSuccess() throws Exception {

        // ---------------------- REQUEST ----------------------
        InterviewQuestionUpdateRequestDto request = new InterviewQuestionUpdateRequestDto(
                List.of(
                        new InterviewQuestionUpdateRequestDto.QuestionUpdateItem(
                                1001L, QuestionType.TECH, "변경된 기술 질문입니다."
                        ),
                        new InterviewQuestionUpdateRequestDto.QuestionUpdateItem(
                                null, QuestionType.CORE, "새로운 코어 질문입니다."
                        )
                ),
                List.of(10L, 20L)  // 삭제할 질문 ID
        );

        // ---------------------- SERVICE MOCK ----------------------
        Mockito.doNothing()
                .when(interviewQuestionService)
                .updateQuestions(eq(1L), any(InterviewQuestionUpdateRequestDto.class));

        // ---------------------- API CALL ----------------------
        mockMvc.perform(
                        put("/api/v1/interviews/{interviewId}/questions", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data")
                        .value("면접 질문이 성공적으로 업데이트되었습니다."));
    }

}
