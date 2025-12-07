package com.server.user.service;

import com.server.global.exception.ApplicationException;
import com.server.user.domain.TestFixtures;
import com.server.user.domain.User;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import com.server.user.util.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private UserService userService;

    // ========== 비밀번호 변경 테스트 ==========

    @Test
    @DisplayName("비밀번호 변경 성공 - 현재 비밀번호가 일치하고, 새 비밀번호가 이전과 다르면 변경됨")
    void changePassword_success() {
        // given
        Long userId = 1L;
        String currentPassword = "CurrentPassword1!";
        String newPassword = "NewPassword1!";

        User user = TestFixtures.createUser("test@company.com", "홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encoded-old-password");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 1) 현재 비밀번호 일치
        given(passwordEncoder.matches(currentPassword, "encoded-old-password"))
                .willReturn(true);

        // 2) 비밀번호 정책 검증 통과
        doNothing().when(passwordValidator)
                .validateForSignup(newPassword, "test@company.com");

        // 3) 새 비밀번호는 이전과 다름
        given(passwordEncoder.matches(newPassword, "encoded-old-password"))
                .willReturn(false);

        // 4) 새 비밀번호 암호화
        given(passwordEncoder.encode(newPassword))
                .willReturn("encoded-new-password");

        userService.changePassword(userId, currentPassword, newPassword);

        verify(passwordValidator).validateForSignup(newPassword, "test@company.com");
        verify(passwordEncoder).encode(newPassword);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호가 일치하지 않으면 예외 발생")
    void changePassword_wrongCurrentPassword_throwsException() {
        // given
        Long userId = 1L;
        String currentPassword = "WrongPassword!";
        String newPassword = "NewPassword1!";

        User user = TestFixtures.createUser("test@company.com", "홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encoded-old-password");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 현재 비밀번호 불일치
        given(passwordEncoder.matches(currentPassword, "encoded-old-password"))
                .willReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userService.changePassword(userId, currentPassword, newPassword));

        assertThat(exception.getErrorCase()).isEqualTo(UserErrorCase.INVALID_PASSWORD);

        verify(passwordValidator, never()).validateForSignup(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 이전 비밀번호와 같으면 예외 발생")
    void changePassword_samePassword_throwsException() {
        // given
        Long userId = 1L;
        String currentPassword = "SamePassword1!";
        String newPassword = "SamePassword1!";

        User user = TestFixtures.createUser("test@company.com", "홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encoded-old-password");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 1) 현재 비밀번호 일치
        given(passwordEncoder.matches(currentPassword, "encoded-old-password"))
                .willReturn(true);

        // 2) 비밀번호 정책 검증 통과
        doNothing().when(passwordValidator)
                .validateForSignup(newPassword, "test@company.com");

        // 3) 새 비밀번호가 이전과 동일
        given(passwordEncoder.matches(newPassword, "encoded-old-password"))
                .willReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userService.changePassword(userId, currentPassword, newPassword));

        assertThat(exception.getErrorCase()).isEqualTo(UserErrorCase.PASSWORD_SAME_AS_OLD);

        verify(passwordEncoder, never()).encode(newPassword);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자를 찾을 수 없음")
    void changePassword_userNotFound_throwsException() {
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userService.changePassword(userId, "current", "new"));

        assertThat(exception.getErrorCase()).isEqualTo(UserErrorCase.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 정책 위반")
    void changePassword_invalidNewPassword_throwsException() {
        // given
        Long userId = 1L;
        String currentPassword = "CurrentPassword1!";
        String newPassword = "weak";

        User user = TestFixtures.createUser("test@company.com", "홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encoded-old-password");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 현재 비밀번호 일치
        given(passwordEncoder.matches(currentPassword, "encoded-old-password"))
                .willReturn(true);

        // 비밀번호 정책 검증 실패!
        doThrow(ApplicationException.from(UserErrorCase.USER_VALIDATION_FAILED))
                .when(passwordValidator)
                .validateForSignup(newPassword, "test@company.com");

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userService.changePassword(userId, currentPassword, newPassword));

        assertThat(exception.getErrorCase()).isEqualTo(UserErrorCase.USER_VALIDATION_FAILED);
    }
}