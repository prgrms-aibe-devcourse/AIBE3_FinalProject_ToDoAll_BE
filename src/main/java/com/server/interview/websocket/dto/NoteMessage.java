package com.server.interview.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoteMessage extends InterviewMessage {

    @NotNull(message = "senderId는 null일 수 없습니다.")
    private Long senderId;

    @NotBlank(message = "sender는 비어 있을 수 없습니다.")
    private String sender;

    @NotBlank(message = "content는 비어 있을 수 없습니다.")
    private String content;

    @NotNull(message = "noteId는 null일 수 없습니다.")
    private Long noteId;

    @Builder
    public NoteMessage(Long interviewId, Long senderId, String sender, String content, Long noteId) {
        super(MessageType.NOTE, interviewId);
        this.senderId = senderId;
        this.sender = sender;
        this.content = content;
        this.noteId = noteId;
    }
}
