package com.server.interview.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoteMessage extends InterviewMessage {

    private Long senderId;
    private String sender;
    private String content;
    private Long noteId;

    public NoteMessage(Long interviewId, Long senderId, String sender, String content, Long noteId) {
        super(MessageType.NOTE, interviewId);
        this.senderId = senderId;
        this.sender = sender;
        this.content = content;
        this.noteId = noteId;
    }
}
