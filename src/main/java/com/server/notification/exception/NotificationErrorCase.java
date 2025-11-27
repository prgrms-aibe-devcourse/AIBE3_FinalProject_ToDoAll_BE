package com.server.notification.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum NotificationErrorCase implements ErrorCase {
    NOT_FOUND(HttpStatus.NOT_FOUND, 10001, "해당 알림을 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 10002, "해당 알림 접근 권한이 없습니다.");

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
