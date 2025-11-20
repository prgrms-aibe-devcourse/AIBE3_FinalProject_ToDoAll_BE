package com.server.interview.websocket.service;

import com.server.interview.websocket.dto.ChatMessage;
import com.server.interview.websocket.dto.NoteMessage;
import com.server.interview.websocket.dto.SystemEventType;
import com.server.interview.websocket.dto.SystemMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewWebSocketService {

    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleUserJoin(Long interviewId, String sessionId, boolean isInterviewer) {
        sessionRegistry.addSession(interviewId, sessionId, isInterviewer);
        log.info("JOIN: sessionId={} interviewId={} interviewer={}", sessionId, interviewId, isInterviewer);

        broadcastSystemMessage(interviewId, SystemEventType.JOIN, "사용자가 입장했습니다.");
    }

    public void handleUserLeave(String sessionId) {
        Long interviewId = sessionRegistry.getInterviewIdBySession(sessionId);
        if(interviewId == null) return;

        sessionRegistry.removeSession(sessionId);
        log.info("LEAVE: sessionId={} interviewId={}", sessionId, interviewId);

        broadcastSystemMessage(interviewId, SystemEventType.LEAVE, "사용자가 퇴장했습니다.");

        if(sessionRegistry.getSessionCount(interviewId) == 0) {
            log.info("INTERVIEW END: interviewId={} 모든 사용자가 퇴장했습니다.", interviewId);
        }
    }

    public void broadcastSystemMessage(Long interviewId, SystemEventType event, String content) {
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/system",
                new SystemMessage(interviewId, event, content)
        );
        log.info("SYSTEM MESSAGE: interviewId={} event={} content={}", interviewId, event, content);
    }


    public void broadcastChatMessage(Long interviewId, ChatMessage message) {
        ChatMessage outgoing = new ChatMessage(
                interviewId,
                message.getSenderId(),
                message.getSender(),
                message.getContent()
        );

        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/chat",
                outgoing
        );
        log.info("CHAT MESSAGE: interviewId={} senderId={} sender={} content={}", interviewId, message.getSenderId(), message.getSender(), message.getContent());
    }

    public void broadcastNoteMessage(Long interviewId, String sessionId, NoteMessage noteMessage) {
        if (!sessionRegistry.isInterviewer(sessionId)) {
            log.warn("NOTE MESSAGE 차단됨 - sessionId={} 는 면접관이 아님", sessionId);
            return;
        }

        NoteMessage outgoing = new NoteMessage(
                interviewId,
                noteMessage.getSenderId(),
                noteMessage.getSender(),
                noteMessage.getContent(),
                noteMessage.getNoteId()
        );

        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/note",
                outgoing
        );
        log.info("NOTE MESSAGE: interviewId={} senderId={} sender={} noteId={}", interviewId, noteMessage.getSenderId(), noteMessage.getSender(), noteMessage.getNoteId());
    }

}
