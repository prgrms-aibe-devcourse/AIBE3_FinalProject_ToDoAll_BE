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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
@Tag(name = "Resume API", description = "이력서 관련 기능을 제공하는 API") // ✅ Swagger 그룹 태그 추가
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
            @ApiResponse(responseCode = "201", description = "이력서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public ResponseEntity<CommonResponse<ResumeResponseDto>> createResume(
            @RequestBody @Valid ResumeCreateRequestDto resume
    ) {
        ResumeResponseDto createdResume = resumeService.createResume(resume);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(createdResume));
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
    @Operation(summary = "이력서 상태 수정", description = "특정 이력서의 상태를 수정합니다. 예: NEW → BOOKMARK, SUBMITTED 등으로 변경할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이력서 상태 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 상태 값 등)"),
            @ApiResponse(responseCode = "404", description = "이력서를 찾을 수 없음")
    })
    public ResponseEntity<CommonResponse<ResumeStatusUpdateResponseDto>> updateResumeStatus(
            @PathVariable Long resumeId,
            @RequestBody @Valid ResumeStatusUpdateDto request
    ) {
        ResumeStatusUpdateResponseDto response = resumeService.updateResumeStatus(resumeId, request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
