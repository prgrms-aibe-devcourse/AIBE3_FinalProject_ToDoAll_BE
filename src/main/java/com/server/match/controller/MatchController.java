package com.server.match.controller;

import com.server.global.response.CommonResponse;
import com.server.match.domain.Match;
import com.server.match.dto.MatchRequestDto;
import com.server.match.dto.MatchResponseDto;
import com.server.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
