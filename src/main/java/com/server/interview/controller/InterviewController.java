package com.server.interview.controller;

import com.server.global.response.CommonResponse;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "InterviewController", description = "API 면접 컨트롤러")
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping
    @Operation(summary = "인터뷰 등록", description = "이력서에 해당하는 인터뷰를 생성합니다.")
    @ApiResponses(value = {
            // 200 SUCCESS
            @ApiResponse(
                    responseCode = "200",
                    description = "인터뷰 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인터뷰 생성 성공",
                                    summary = "Success Example",
                                    value = """
                                    {
                                        "message": "success",
                                        "data": {
                                            "interviewId": 1
                                        }
                                    }
                                    """
                            )
                    )
            ),
            // 404 FAILED
            @ApiResponse(
                    responseCode = "404",
                    description = "JD 또는 Resume 를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "JD 없음",
                                            value = """
                                            {
                                                "errorCode": 6001,
                                                "message": "해당 채용공고를 찾을 수 없습니다."
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Resume 없음",
                                            value = """
                                            {
                                                "errorCode": 4041,
                                                "message": "해당 이력서를 찾을 수 없습니다."
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    public CommonResponse<InterviewCreateResponseDto> createInterview (
            @RequestBody InterviewCreateRequestDto interviewCreateRequestDto
    ){
        InterviewCreateResponseDto interviewCreateResponseDto = interviewService.create(interviewCreateRequestDto);
        return CommonResponse.success(interviewCreateResponseDto);
    }
}
