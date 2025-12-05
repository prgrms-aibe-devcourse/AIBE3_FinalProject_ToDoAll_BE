package com.server.global.exception;

import com.server.global.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<CommonResponse<?>> handleApplicationException(ApplicationException e) {
        log.warn("[ApplicationException] {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCase().getHttpStatusCode())
                .body(CommonResponse.error(e.getErrorCase()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("요청 값이 유효하지 않습니다.");

        log.warn("[ValidationException] {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(4001, message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResponse<?>> handleAuthenticationException(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(401, "로그인이 필요합니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[MethodNotAllowed] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(CommonResponse.error(405, "허용되지 않은 HTTP 메소드입니다."));
    }

    // 정적 리소스 없음 → 404로 분리 처리

    @ExceptionHandler(NoResourceFoundException.class) // 정적 리소스 핸들러가 파일을 찾지 못했을 때
    public ResponseEntity<CommonResponse<?>> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        String uri = request.getRequestURI(); // 요청 URI 확인 (어떤 경로에서 에러났는지 로그용)
        log.warn("[NoResourceFoundException] uri={}, message={}", uri, e.getMessage()); // 경고 로그 (500이 아닌 404 상황)

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404 Not Found로 응답
                .body(CommonResponse.error(404, "요청하신 리소스를 찾을 수 없습니다.")); // 사용자에게는 리소스 없음 메시지
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleUnexpectedException(Exception e, HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/actuator")) {
            throw e;
        }

        log.error("[UnexpectedException] {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(500, "서버 내부 오류가 발생했습니다."));
    }
}