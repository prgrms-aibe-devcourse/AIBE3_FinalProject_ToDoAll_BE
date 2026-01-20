package com.server.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.auth.config.AuthCookieProperties;
import com.server.auth.dto.PasswordResetConfirmRequestDto;
import com.server.auth.dto.PasswordResetRequestDto;
import com.server.auth.dto.PasswordResetResponseDto;
import com.server.auth.dto.TokenRefreshRequestDto;
import com.server.auth.service.AuthService;
import com.server.auth.service.PasswordResetService;
import com.server.user.dto.UserLoginRequestDto;
import com.server.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    PasswordResetService passwordResetService;

    @MockitoBean
    AuthCookieProperties authCookieProperties;

    @BeforeEach
    void setUp() {
        //AuthCookieProperties mock 값 세팅
        when(authCookieProperties.isSecure()).thenReturn(false);
        when(authCookieProperties.getSameSite()).thenReturn("Lax");
        when(authCookieProperties.getPath()).thenReturn("/");
        when(authCookieProperties.getAccessMaxAgeSeconds()).thenReturn(3600L);
        when(authCookieProperties.getRefreshMaxAgeSeconds()).thenReturn(604800L);

    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "test@company.com",
                "Password1!"
        );

        UserLoginResponseDto responseDto = UserLoginResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(authService.login(anyString(), anyString()))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        hasItem(containsString("refreshToken="))));
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰 재발급 성공")
    void refresh_success() throws Exception {
        TokenRefreshRequestDto requestDto = new TokenRefreshRequestDto("old-refresh-token");

        UserLoginResponseDto responseDto = UserLoginResponseDto.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authService.reissueAccessToken("old-refresh-token"))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/api/v1/auth/token/refresh")
                                .cookie(new Cookie("refreshToken", "old-refresh-token"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        hasItem(containsString("refreshToken="))));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공")
    void sendPasswordResetEmail_success() throws Exception {
        PasswordResetRequestDto requestDto =
                new PasswordResetRequestDto("test@company.com");

        PasswordResetResponseDto responseDto =
                PasswordResetResponseDto.ofEmailSent();

        when(passwordResetService.sendPasswordResetEmail(any()))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/api/v1/auth/password/reset-requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.message")
                        .value("비밀번호 재설정 링크가 이메일로 발송되었습니다. (유효시간: 30분)"));
    }


    @Test
    @DisplayName("비밀번호 재설정 완료 성공")
    void confirmPasswordReset_success() throws Exception {
        PasswordResetConfirmRequestDto requestDto =
                new PasswordResetConfirmRequestDto("dummy-token", "NewPassword1!");

        PasswordResetResponseDto responseDto =
                PasswordResetResponseDto.ofPasswordChanged();

        when(passwordResetService.resetPassword(any()))
                .thenReturn(responseDto);
        mockMvc.perform(
                        post("/api/v1/auth/password/reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.message")
                        .value("비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요."));
    }


    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        TokenRefreshRequestDto requestDto = new TokenRefreshRequestDto("refresh-token");
        doNothing().when(authService).logout("refresh-token");

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("로그아웃 되었습니다."));
    }
}

