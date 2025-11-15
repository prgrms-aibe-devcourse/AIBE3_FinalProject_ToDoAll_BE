package com.server.user.exception;

import com.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCase implements ErrorCase {

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1003, "사용자를 찾을 수 없습니다."),

    // 409 - 중복/충돌
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, 1001, "이미 가입된 이메일입니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.CONFLICT, 1004, "비밀번호가 일치하지 않습니다."),

    // 400 - 요청 형식/정책 위반

    USER_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 3001, "요청 데이터 형식이 올바르지 않습니다."),
    PASSWORD_EQUALS_EMAIL_ID(HttpStatus.BAD_REQUEST, 3003, "비밀번호에 이메일 아이디를 그대로 사용할 수 없습니다."),
    // 비밀번호에 금지어 포함
    PASSWORD_CONTAINS_FORBIDDEN_WORD(HttpStatus.BAD_REQUEST, 3004, "비밀번호에 사용할 수 없는 단어가 포함되어 있습니다."),



    // 401 - 인증 필요
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 2001, "인증이 필요합니다."),

    // 400 - 허용되지 않은 성별, 생일 등
    INVALID_USER_FIELD(HttpStatus.BAD_REQUEST, 3002, "허용되지 않은 사용자 정보입니다.");


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
