package com.server.jd.controller;


import com.server.global.response.CommonResponse;
import com.server.jd.dto.*;
import com.server.jd.service.JobDescriptionService;
import com.server.jd.service.SkillQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jd")
@Tag(name = "JobDescriptionController", description = "API JD 컨트롤러")
public class JobDescriptionController {
    private final JobDescriptionService jobService;
    private final SkillQueryService skillQueryService;

    @GetMapping
    public ResponseEntity<CommonResponse<Page<JobDescriptionListResponseDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size
    ) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(CommonResponse.success(jobService.getMyList(pageable, 5)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<JobDescriptionDetailResponseDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success(jobService.getDetail(id)));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createDraft(@Valid @RequestBody JobDescriptionCreateRequestDto request) {
        Long id = jobService.createDraft(request);
        return ResponseEntity
                .created(URI.create("/api/v1/jd/" + id))
                .body(CommonResponse.success(id));
    }

    @PatchMapping(value = "/{id}/thumbnail", consumes = {"multipart/form-data"})
    @Operation(summary = "특정 JD에 썸네일 이미지 업로드 및 연결 (S3 File Key 업데이트)")
    public ResponseEntity<CommonResponse<String>> updateThumbnail(
            @PathVariable Long id,
            @RequestPart("thumbnailFile") MultipartFile thumbnailFile
    ) {
        // 서비스에서 파일 업로드 및 엔티티 업데이트 후, S3 File Key 반환
        String newFileKey = jobService.updateThumbnail(id, thumbnailFile);
        System.out.println("newFileKey : " + newFileKey);
        return ResponseEntity.ok(CommonResponse.success(newFileKey));
    }

    @GetMapping("/skills")
    public ResponseEntity<CommonResponse<List<SkillResponseDto>>> getSkills() {
        List<SkillResponseDto> Skills = skillQueryService.getSkills();
        return ResponseEntity.ok(CommonResponse.success(Skills));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponse<JobDescriptionStatusResponseDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody JobDescriptionStatusRequestDto request
    ) {
        JobDescriptionStatusResponseDto dto = jobService.updateStatus(id, request);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<JobDescriptionDetailResponseDto>> update(
            @PathVariable Long id,
            @RequestBody JobDescriptionUpdateRequestDto request
    ) {
        JobDescriptionDetailResponseDto result = jobService.update(id, request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @GetMapping("/interview/options")
    @Operation(summary = "사용자가 참여한 인터뷰의 JD 목록 조회")
    public CommonResponse<List<JobDescriptionInterviewOptionDto>> getMyInterviewJdList(
    ) {
        return CommonResponse.success(jobService.getMyInterviewOptionJdList());
    }

    @GetMapping("/options")
    @Operation(summary = "사용자가 만든 인터뷰의 JD 목록 조회")
    public CommonResponse<List<JobDescriptionOptionDto>> getMyJdList(
    ) {
        return CommonResponse.success(jobService.getMyOptionJdList());
    }
}
