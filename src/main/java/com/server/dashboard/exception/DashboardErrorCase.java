package com.server.dashboard.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DashboardErrorCase implements ErrorCase {
    DASHBOARD_QUERY_FAIL(HttpStatus.UNPROCESSABLE_ENTITY, 12001, "DB Query에 실패했습니다."),
    DASHBOARD_INVALID_STATUS_VALUE(HttpStatus.UNPROCESSABLE_ENTITY, 12002, "Enum 상태 중 없는 값이 Query 되었습니다.");

    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override
    public Integer getHttpStatusCode() {
        return httpStatus.value();
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
