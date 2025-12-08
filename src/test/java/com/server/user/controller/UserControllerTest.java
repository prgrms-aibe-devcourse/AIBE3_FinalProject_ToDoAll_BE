package com.server.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.global.exception.ApplicationException;
import com.server.user.domain.Gender;
import com.server.user.dto.*;
import com.server.user.exception.UserErrorCase;
import com.server.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // @MockBean 대체
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)   // ★ 보안 필터/CSRF 비활성화
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    // 1. 회원가입 성공 테스트
    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        UserSignupRequestDto requestDto = UserSignupRequestDto.of(
                "dummy-token",
                "test@company.com",
                "Password1!",
                "Password1!",
                "홍길동",
                "길동이",
                "테스트회사",
                "백엔드 개발자"
        );

        UserSignupResponseDto responseDto = new UserSignupResponseDto(
                1L,
                "test@company.com",
                "홍길동",
                "길동이",
                "테스트회사",
                "백엔드 개발자",
                LocalDateTime.now()
        );

        when(userService.signup(any(UserSignupRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@company.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.companyName").value("테스트회사"))
                .andExpect(jsonPath("$.data.position").value("백엔드 개발자"));
    }

    // 1-1 회원가입 예외 - 중복 이메일
    @Test
    @DisplayName("회원가입 실패")
    void signup_duplicateEmail_conflict() throws Exception {
        UserSignupRequestDto requestDto = UserSignupRequestDto.of(
                "dummy-token",
                "test@company.com",
                "Password1!",
                "Password1!",
                "홍길동",
                "길동이",
                "테스트회사",
                "백엔드 개발자"
        );
        when(userService.signup(any(UserSignupRequestDto.class)))
                .thenThrow(new ApplicationException(UserErrorCase.USER_ALREADY_EXISTS));

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isConflict());
    }

    // 2. 내 정보 조회
    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyProfile_success() throws Exception {
        Long userId = 1L;

        setAuthentication(userId);

        UserProfileResponseDto profile = new UserProfileResponseDto(
                userId,
                "test@company.com",
                "홍길동",
                "길동이",
                "테스트회사",
                "백엔드 개발자",
                "010-1234-5678",
                LocalDate.of(1999, 2, 15),
                Gender.FEMALE,
                "https://example.com/profile.png"
        );

        when(userService.getMyProfile(userId))
                .thenReturn(profile);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@company.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.companyName").value("테스트회사"))
                .andExpect(jsonPath("$.data.position").value("백엔드 개발자"));
    }

    // 3. 비밀번호 변경 성공
    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        Long userId = 1L;
        setAuthentication(userId);

        ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto(
                "CurrentPassword1!",
                "NewPassword1!"
        );

        mockMvc.perform(
                        patch("/api/v1/users/me/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                // CommonResponse.success("비밀번호가 변경되었습니다.") 기준
                .andExpect(jsonPath("$.data").value("비밀번호가 변경되었습니다."));
    }

    // 4. 비밀번호 변경 실패
    @Test
    @DisplayName("비밀번호 변경 실패")
    void changePassword_invalidCurrentPassword() throws Exception {
        Long userId = 1L;
        setAuthentication(userId);

        ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto(
                "WrongPassword1!",
                "NewPassword1!"
        );

        doThrow(new ApplicationException(UserErrorCase.INVALID_PASSWORD))
                .when(userService)
                .changePassword(eq(userId), any(), any());

        mockMvc.perform(
                        patch("/api/v1/users/me/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(4012))
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));
    }

    // 5. 내 정보 수정 성공
    @Test
    @DisplayName("내 정보 수정 성공")
    void updateMyProfile_success() throws Exception {
        Long userId = 1L;
        setAuthentication(userId);

        String requestJson = """
                {
                  "name": "홍길동",
                  "nickname": "길동이",
                  "position": "백엔드 개발자",
                  "phoneNumber": "010-1234-5678",
                  "birthDate": "1999-02-15",
                  "gender": "FEMALE"
                }
                """;

        UserProfileResponseDto updatedProfile = new UserProfileResponseDto(
                userId,
                "test@company.com",
                "홍길동",
                "길동이",
                "테스트회사",
                "백엔드 개발자",
                "010-1234-5678",
                LocalDate.of(1999, 2, 15),
                Gender.FEMALE,
                "https://example.com/profile.png"
        );

        when(userService.updateMyProfile(eq(userId), any(UserProfileUpdateRequestDto.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(
                        patch("/api/v1/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.position").value("백엔드 개발자"));
    }
    // 6. 프로필 수정 실패
    @Test
    @DisplayName("내 정보 수정 실패")
    void updateMyProfile_userNotFound() throws Exception {
        Long userId = 99L;  // 존재하지 않는 유저 ID
        setAuthentication(userId);

        String requestJson = """
            {
              "name": "홍길동",
              "nickname": "길동이",
              "position": "백엔드 개발자",
              "phoneNumber": "010-1234-5678",
              "birthDate": "1999-02-15",
              "gender": "FEMALE"
            }
            """;

        when(userService.updateMyProfile(eq(userId), any()))
                .thenThrow(new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        mockMvc.perform(
                        patch("/api/v1/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound());
    }





    private void setAuthentication(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}