package com.server.interview.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SystemMessage extends InterviewMessage {

    @NotNull(message = "interviewId는 null일 수 없습니다.")
    private SystemEventType event;

    @NotBlank(message = "content는 비어있을 수 없습니다.")
    private String content;

    @Builder
    public SystemMessage(Long interviewId, SystemEventType event, String content) {
        super(MessageType.SYSTEM, interviewId);
        this.event = event;
        this.content = content;
    }
}
