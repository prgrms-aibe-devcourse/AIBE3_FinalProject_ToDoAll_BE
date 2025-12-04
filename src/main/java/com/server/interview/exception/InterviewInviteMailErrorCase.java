package com.server.interview.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InterviewInviteMailErrorCase implements ErrorCase {

    INVALID_RECIPIENT_EMAIL(HttpStatus.BAD_REQUEST, 9501, "수신자 이메일 형식이 올바르지 않습니다."),
    INVALID_INVITE_URL(HttpStatus.BAD_REQUEST, 9502, "초대 링크 형식이 올바르지 않습니다."),
    INVALID_EXPIRY_MINUTES(HttpStatus.BAD_REQUEST, 9503, "유효시간 값이 올바르지 않습니다."),

    MAIL_TEMPLATE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, 9511, "메일 템플릿을 찾을 수 없습니다."),
    MAIL_TEMPLATE_RENDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 9512, "메일 템플릿 렌더링에 실패했습니다."),
    MAIL_COMPOSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 9513, "메일 생성에 실패했습니다."),
    MAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, 9514, "메일 전송에 실패했습니다.");


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

