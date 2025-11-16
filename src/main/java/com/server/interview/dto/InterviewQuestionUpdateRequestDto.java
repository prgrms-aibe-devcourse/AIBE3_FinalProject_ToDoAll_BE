package com.server.interview.dto;

import com.server.interview.domain.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InterviewQuestionUpdateRequestDto(
        @NotEmpty(message = "변경사항이 없습니다.")
        List<QuestionUpdateItem> questions,

        @Schema(description = "삭제할 질문 ID", example = "[1,4,5")
        List<Long> deleteQuestionIds
) {
    public record QuestionUpdateItem(
            @Schema(description = "질문 ID", example = "1")
            Long questionId,       // null -> 새 질문

            @Schema(description = "질문 유형", example = "CORE")
            @NotBlank(message = "질문유형을 입력해주세요.")
            QuestionType questionType,   // CORE, TECH, BEHAVIOR

            @Schema(description = "질문 내용", example = "새로운 코어 질문입니다.")
            @NotBlank(message = "질문 내용을 입력해주세요.")
            String content
    ) {}
}
