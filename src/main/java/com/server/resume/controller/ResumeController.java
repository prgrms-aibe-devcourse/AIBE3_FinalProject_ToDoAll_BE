package com.server.resume.controller;

import com.server.global.response.CommonResponse;
import com.server.resume.dto.ResumeCreateRequestDto;
import com.server.resume.dto.ResumeResponseDto;
import com.server.resume.dto.ResumeStatusUpdateDto;
import com.server.resume.dto.ResumeStatusUpdateResponseDto;
import com.server.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/{resumeId}")
    @Operation(summary = "이력서 상세 조회", description = "특정 이력서의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public ResponseEntity<CommonResponse<ResumeResponseDto>> getResume(@PathVariable Long resumeId) {
        return ResponseEntity.ok(CommonResponse.success(resumeService.getResumeById(resumeId)));
    }

    @PostMapping("")
    @Operation(summary = "이력서 생성", description = "새로운 이력서를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이력서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public ResponseEntity<CommonResponse<ResumeResponseDto>> createResume(@RequestBody ResumeCreateRequestDto resume) {
        return ResponseEntity.ok(CommonResponse.success(resumeService.createResume(resume)));
    }

    @DeleteMapping("/{resumeId}")
    @Operation(summary = "이력서 삭제", description = "특정 이력서를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이력서 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public ResponseEntity<CommonResponse<String>> deleteResume(@PathVariable Long resumeId) {
        resumeService.deleteResume(resumeId);
        return ResponseEntity.ok(CommonResponse.success("이력서 삭제 성공"));
    }

    @PatchMapping("/{resumeId}/status")
    public ResponseEntity<CommonResponse<ResumeStatusUpdateResponseDto>> updateResumeStatus(
            @PathVariable Long resumeId,
            @RequestBody @Valid ResumeStatusUpdateDto request
    ) {
        var response = resumeService.updateResumeStatus(resumeId, request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

}