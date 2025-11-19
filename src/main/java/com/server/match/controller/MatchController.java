package com.server.match.controller;

import com.server.global.exception.ApplicationException;
import com.server.global.response.CommonResponse;
import com.server.match.domain.Match;
import com.server.match.domain.MatchSortType;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.*;
import com.server.match.exception.MatchErrorCase;
import com.server.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    @Operation(summary = "JD에 지원자 매칭 정보 등록", description = "JD에 특정 이력서가 지원하도록 매칭 정보를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 등록 성공"),
            @ApiResponse(responseCode = "404", description = "채용공고 또는 이력서를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 매칭된 정보가 존재함")
    })
    public CommonResponse<MatchResponseDto> registerMatch(
            @RequestBody @Valid MatchRequestDto dto
    ) {
        Match match = matchService.registerMatch(dto);
        MatchResponseDto response = new MatchResponseDto(match.getId(), match.getStatus());
        return CommonResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "JD에 지원한 전체 이력서 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public CommonResponse<List<MatchListResponseDto>> getMatchedResumes(
            @RequestParam @NotNull Long jdId,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false, defaultValue = "LATEST") MatchSortType sort,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        if (jdId == null || jdId <= 0) {
            throw new ApplicationException(MatchErrorCase.JD_INVALID_ID);
        }

        MatchSearchCondition condition = new MatchSearchCondition(jdId, status, sort, limit, offset);
        List<MatchListResponseDto> result = matchService.getMatchedResumes(condition);
        return CommonResponse.success(result);
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "JD + 이력서 매칭 상세 조회", description = "특정 매칭(matchId)에 대한 상세 분석 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "매칭 정보를 찾을 수 없음")
    })
    public CommonResponse<MatchDetailResponseDto> getMatchDetail(@PathVariable Long matchId) {
        MatchDetailResponseDto detail = matchService.getMatchDetail(matchId);
        return CommonResponse.success(detail);
    }
}