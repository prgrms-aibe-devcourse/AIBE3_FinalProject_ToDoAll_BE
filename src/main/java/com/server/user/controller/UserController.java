package com.server.user.controller;

import com.server.global.response.CommonResponse;
import com.server.user.dto.UserSignupRequestDto;
import com.server.user.dto.UserSignupResponseDto;
import com.server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<CommonResponse<UserSignupResponseDto>> signup(
            @Valid @RequestBody UserSignupRequestDto request
    ) {
        UserSignupResponseDto response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success(response));
    }
}
