//package com.server.resume.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.server.global.exception.ApplicationException;
//import com.server.resume.controller.ResumeController;
//import com.server.resume.dto.*;
//import com.server.resume.exception.ResumeErrorCase;
//import com.server.resume.service.ResumeService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDate;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ResumeController.class)
//class ResumeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ResumeService resumeService;
//
//    // ================================================================
//    // 1) DTO 유효성 검증
//    // ================================================================
//    @Test
//    @DisplayName("이력서 생성 실패 - 필수 필드 누락 시 400")
//    void createResume_missingField_shouldReturn400() throws Exception {
//
//        // jobDescriptionId 누락 → 실패해야 함
//        ResumeCreateRequestDto request = new ResumeCreateRequestDto(
//                null,                      // JD ID
//                "홍길동",                    // name
//                "M",                        // gender
//                LocalDate.of(1995, 1, 1),   // birthDate
//                "test@test.com",            // email
//                "01012345678",              // phone
//                "서울시",                    // address
//                "101호",                    // detailAddress
//                null,                       // resumeFileUrl
//                null,                       // portfolioFileUrl
//                null,                       // education
//                null,                       // experience
//                null,                       // skills
//                null                        // activities
//        );
//
//        mockMvc.perform(post("/api/v1/resumes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ================================================================
//    // 2) 존재하지 않는 JD 테스트
//    // ================================================================
//    @Test
//    @DisplayName("이력서 생성 실패 - 존재하지 않는 JobDescription")
//    void createResume_jobDescriptionNotFound() throws Exception {
//
//        Mockito.when(resumeService.createResume(any()))
//                .thenThrow(new ApplicationException(ResumeErrorCase.JD_NOT_FOUND));
//
//        ResumeCreateRequestDto request = new ResumeCreateRequestDto(
//                999L,
//                "홍길동",
//                "M",
//                LocalDate.of(1995, 1, 1),
//                "test@test.com",
//                "01012345678",
//                "서울시",
//                "101호",
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//
//        mockMvc.perform(post("/api/v1/resumes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound());
//    }
//
//    // ================================================================
//    // 3) 잘못된 상태 업데이트 테스트
//    // ================================================================
//    @Test
//    @DisplayName("이력서 상태 수정 실패 - 잘못된 상태 값")
//    void updateResumeStatus_invalidStatus_shouldReturn400() throws Exception {
//
//        Mockito.when(resumeService.updateResumeStatus(eq(1L), any()))
//                .thenThrow(new ApplicationException(ResumeErrorCase.INVALID_STATUS));
//
//        ResumeStatusUpdateDto request = new ResumeStatusUpdateDto("INVALID_STATUS");
//
//        mockMvc.perform(patch("/api/v1/resumes/1/status")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ================================================================
//    // 4) 정상 상태 업데이트 테스트
//    // ================================================================
//    @Test
//    @DisplayName("이력서 상태 수정 성공")
//    void updateResumeStatus_success() throws Exception {
//
//        ResumeStatusUpdateResponseDto response =
//                new ResumeStatusUpdateResponseDto(1L, "SUBMITTED");
//
//        Mockito.when(resumeService.updateResumeStatus(eq(1L), any()))
//                .thenReturn(response);
//
//        ResumeStatusUpdateDto request = new ResumeStatusUpdateDto("SUBMITTED");
//
//        mockMvc.perform(patch("/api/v1/resumes/1/status")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
//    }
//}
