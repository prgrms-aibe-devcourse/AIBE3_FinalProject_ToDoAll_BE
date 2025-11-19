package com.server.interview.websocket.service;

import com.server.interview.websocket.dto.InterviewMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class InterviewWebSocketService {

    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleUserJoin(Long interviewId, String sessionId) {
        sessionRegistry.addSession(interviewId, sessionId);
        System.out.println("Join: " + sessionId + " -> interview " + interviewId);

        broadcastSystemMessage(interviewId, "JOIN", "사용자가 입장했습니다.");
    }

    public void handleUserLeave(String sessionId) {
        Long interviewId = sessionRegistry.getInterviewIdBySession(sessionId);
        if(interviewId != null) return;

        sessionRegistry.removeSession(sessionId);
        System.out.println("Leave: " + sessionId + "  -> interview " + interviewId);

        broadcastSystemMessage(interviewId, "LEAVE", "사용자가 퇴장했습니다.");

        if(sessionRegistry.getSessionCount(interviewId) == 0) {
            System.out.println("인터뷰 종료: " + interviewId + " 모든 사용자가 퇴장했습니다.");
        }
    }

    public void broadcastSystemMessage(Long interviewId, String event, String content) {
        InterviewMessage message = InterviewMessage.of(
                interviewId,
                event,
                content
        );

        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/system",
                message
        );
    }

}
