package com.server.s3.controller;

import com.server.global.response.CommonResponse;
import com.server.s3.service.S3Uploader;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@Validated
@Tag(name = "S3 API", description = "S3 컨트롤러")
public class S3Controller {
    private final S3Uploader s3Uploader;

    @GetMapping("/download")
    @Operation(summary = "presigned URL 발급", description = "이번 주 예정된 면접 일정")
    public CommonResponse<String> getDownloadUrl (
        @RequestParam @NotBlank String fileKey
    ) {
        String url = s3Uploader.createPresignedGetUrl(fileKey);
        return CommonResponse.success(url);
    }
}
