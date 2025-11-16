package com.server.auth.exception;

import com.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCase implements ErrorCase {

    // 401 - 인증 실패
    AUTH_INVALID_CREDENTIAL(HttpStatus.UNAUTHORIZED, 2002, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 2001, "유효하지 않은 액세스 토큰입니다."),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 2003, "유효하지 않은 리프레시 토큰입니다."),
    AUTH_INVALID_PASSWORD_RESET_TOKEN(HttpStatus.UNAUTHORIZED, 2004, "비밀번호 재설정 토큰이 만료되었거나 유효하지 않습니다."),
    EMAIL_AUTH_ALREADY_SENT(HttpStatus.TOO_MANY_REQUESTS, 2105, "이미 인증 메일이 발송되었습니다. 잠시 후 다시 시도해주세요."),
    AUTH_EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 4015, "만료된 리프레시 토큰입니다."),
    AUTH_REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, 4016, "리프레시 토큰 정보가 일치하지 않습니다."),



    // 404
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1003, "사용자를 찾을 수 없습니다."),

    // 429
    AUTH_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, 4001, "요청이 너무 자주 발생했습니다. 잠시 후 다시 시도해주세요."),

    // 422 - 이메일 도메인 규칙 위반
    EMAIL_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, 3002, "허용되지 않은 이메일 도메인입니다."),

    // 400 - 검증 실패
    AUTH_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 3001, "요청 데이터 형식이 올바르지 않습니다."),
    EMAIL_AUTH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, 2101, "유효하지 않은 이메일 인증 토큰입니다."),
    EMAIL_AUTH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, 2102, "만료된 이메일 인증 토큰입니다."),
    EMAIL_AUTH_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, 2103, "이미 인증이 완료된 토큰입니다."),


    // 500 - 서버 오류
    AUTH_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 9999, "서버 내부 오류가 발생했습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 9001, "이메일 전송에 실패했습니다.");



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

