package com.server.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.global.exception.ApplicationException;
import com.server.resume.domain.ResumeStatus;
import com.server.resume.dto.*;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.service.ResumeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeService resumeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("POST /api/v1/resumes - 생성 성공")
    void getResume_success() throws Exception {
        ResumeResponseDto dto = new ResumeResponseDto(
                1L,
                10L,
                "백엔드 개발자",
                "홍길동",
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
                "portfolio-url",
                ResumeStatus.NEW
        );

        Mockito.when(resumeService.getResumeById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/resumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("GET /api/v1/resumes - 조회 실패")
    void getResume_fail() throws Exception {
        ApplicationException applicationException = new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND);

        Mockito.when(resumeService.getResumeById(999L))
                .thenThrow(applicationException);

        mockMvc.perform(get("/api/v1/resumes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(4041))
                .andExpect(jsonPath("$.message").value("해당 이력서를 찾을 수 없습니다."));;
    }


    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("POST /api/v1/resumes - 생성 성공")
    void createResume_success() throws Exception {
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

        ResumeResponseDto response = new ResumeResponseDto(
                1L,
                10L,
                "백엔드 개발자",
                "홍길동",
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
                "portfolio-url",
                ResumeStatus.NEW
        );

        Mockito.when(resumeService.createResume(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/resumes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("POST /api/v1/resumes - 유효성 검증 실패(이름)")
    void createResume_validationFail1() throws Exception {

        ResumeCreateRequestDto invalidRequest = new ResumeCreateRequestDto(
                "",
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

        mockMvc.perform(post("/api/v1/resumes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(4001))
                .andExpect(jsonPath("$.message").value("이름은 필수입니다."));
        ;

    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("POST /api/v1/resumes - 유효성 검증 실패(지원 직무)")
    void createResume_validationFail() throws Exception {

        ResumeCreateRequestDto invalidRequest = new ResumeCreateRequestDto(
                "홍길동",
                null,
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

        mockMvc.perform(post("/api/v1/resumes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(4001))
                .andExpect(jsonPath("$.message").value("지원 직무는 필수입니다."));
        ;

    }


    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("DELETE /api/v1/resumes/{id} - 삭제 성공")
    void deleteResume_success() throws Exception {
        Mockito.doNothing().when(resumeService).deleteResume(1L);

        mockMvc.perform(delete("/api/v1/resumes/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("이력서 삭제 성공"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("PATCH /api/v1/resumes/{id}/status - 상태 변경 성공")
    void updateResumeStatus_success() throws Exception {

        ResumeStatusUpdateDto requestDto = new ResumeStatusUpdateDto(ResumeStatus.BOOKMARK);

        ResumeStatusUpdateResponseDto responseDto =
                new ResumeStatusUpdateResponseDto(1L, ResumeStatus.BOOKMARK);

        Mockito.when(resumeService.updateResumeStatus(eq(1L), any()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/resumes/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BOOKMARK"));
    }
}
