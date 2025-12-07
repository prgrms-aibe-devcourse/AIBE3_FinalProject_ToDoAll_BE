package com.server.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.auth.dto.EmailAuthCompleteResponseDto;
import com.server.auth.dto.EmailAuthSendRequestDto;
import com.server.auth.service.EmailAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailAuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    EmailAuthService emailAuthService;

    @Test
    @DisplayName("회사 이메일 인증 메일 발송 성공 - 200과 message=success, data=null 반환")
    void sendEmailAuth_success() throws Exception {
        // given
        EmailAuthSendRequestDto requestDto = EmailAuthSendRequestDto.of("test@company.com");

        // 실제로는 void 메서드라 when(...) 필요 없음
        // when(emailAuthService.sendAuthEmail(any())).thenReturn(...);  // X

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/email-verifications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("이메일 인증 완료 조회 성공 - 200과 이메일/시간 반환")
    void completeEmailAuth_success() throws Exception {
        // given
        String token = "dummy-token";

        EmailAuthCompleteResponseDto responseDto =
                new EmailAuthCompleteResponseDto(
                        "test@company.com",
                        LocalDateTime.of(2025, 1, 1, 0, 0)
                );

        when(emailAuthService.completeAuth(token))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        get("/api/v1/auth/email-verifications/complete")
                                .param("token", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.email").value("test@company.com"))
                .andExpect(jsonPath("$.data.verifiedAt").exists());
    }
}
