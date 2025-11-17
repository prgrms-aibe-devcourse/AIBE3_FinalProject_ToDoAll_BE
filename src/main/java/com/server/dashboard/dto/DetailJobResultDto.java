package com.server.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;

public enum JobStatus {
    DOCUMENT, // 지원서 접수 중
    INTERVIEW, // 면접 진행 중
    FINISHED // 모든 일정 완료
}

public record DetailJobResultDto(
        String title,
        ArrayList<Integer> slotData,
        JobStatus status
) {

}
