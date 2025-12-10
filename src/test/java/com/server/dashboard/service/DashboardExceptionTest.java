package com.server.dashboard.service;

import com.server.dashboard.dto.CountByStatusInterface;
import com.server.dashboard.exception.DashboardErrorCase;
import com.server.dashboard.repository.DashboardInterviewRepository;
import com.server.dashboard.repository.DashboardJobRepository;
import com.server.global.exception.ApplicationException;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DashboardExceptionTest {
    @Mock
    private DashboardJobRepository dashboardJobRepository;

    @Mock
    private DashboardInterviewRepository dashboardInterviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("userId가 null 일 경우 UNAUTHORIZED 예외")
    public void validateUserExists1() {
        // when
        ApplicationException ae = assertThrows(
                ApplicationException.class,
                ()->dashboardService.validateUserExists(null)
        );

        // then
        assertThat(ae.getErrorCase()).isEqualTo(UserErrorCase.UNAUTHORIZED);
    }

    @Test
    @DisplayName("미등록 userId 일 경우 USER_NOT_FOUND 예외")
    public void validateUserExists2() {
        // given
        given(userRepository.existsById(anyLong())).willReturn(false);

        // when
        ApplicationException ae = assertThrows(
                ApplicationException.class,
                ()->dashboardService.validateUserExists(1L)
        );

        // then
        assertThat(ae.getErrorCase()).isEqualTo(UserErrorCase.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("status 문자열이 JobStatus 타입이 아닐 경우 DASHBOARD_INVALID_STATUS_VALUE 예외")
    public void getCountByJobStatus() {
        // given
        given(userRepository.existsById(anyLong())).willReturn(true);
        List<CountByStatusInterface> list =  new ArrayList<>(Arrays.asList(1, 2, 3)).stream().map(
                num-> mock(CountByStatusInterface.class)
        ).toList();

        given(dashboardJobRepository.findCountByJobStatus(anyLong()))
                .willReturn(list);

        // when
        ApplicationException ae = assertThrows(
                ApplicationException.class,
                ()->dashboardService.getCountByJobStatus(1L)
        );

        // then
        assertThat(ae.getErrorCase()).isEqualTo(DashboardErrorCase.DASHBOARD_INVALID_STATUS_VALUE);
    }

    @Test
    @DisplayName("status 문자열이 InterviewStatus 타입이 아닐 경우 DASHBOARD_INVALID_STATUS_VALUE 예외")
    public void getCountByInterviewStatus() {
        // given
        given(userRepository.existsById(anyLong())).willReturn(true);
        List<CountByStatusInterface> list =  new ArrayList<>(Arrays.asList(1, 2, 3)).stream().map(
                num-> mock(CountByStatusInterface.class)
        ).toList();

        given(dashboardInterviewRepository.findCountByInterviewStatus(anyLong()))
                .willReturn(list);

        // when
        ApplicationException ae = assertThrows(
                ApplicationException.class,
                ()->dashboardService.getCountByInterviewStatus(1L)
        );

        // then
        assertThat(ae.getErrorCase()).isEqualTo(DashboardErrorCase.DASHBOARD_INVALID_STATUS_VALUE);
    }
}
