package com.server.interview.websocket.service;

import com.server.interview.dto.InterviewNoteMemoCreateRequestDto;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.service.InterviewNoteMemoService;
import com.server.interview.service.InterviewService;
import com.server.interview.websocket.domain.ChatMessageEntity;
import com.server.interview.websocket.dto.*;
import com.server.interview.websocket.registry.SessionRegistry;
import com.server.interview.websocket.repository.ChatMessageRepository;
import com.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewWebSocketService {

    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final InterviewNoteMemoService interviewNoteMemoService;
    private final UserService userService;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final InterviewService interviewService;



    // 사용자 입장
    public void handleUserJoin(Long interviewId, Long userId, String sessionId) {
        boolean isInterviewer = false;
        if (userId != null && userId != -1L) {

            isInterviewer = interviewParticipantRepository
                    .existsByInterviewIdAndUserId(interviewId, userId);
        }
        sessionRegistry.addSession(interviewId, userId, sessionId, isInterviewer);
        log.info("JOIN: sessionId={} interviewId={} interviewer={}", sessionId, interviewId, isInterviewer);

        broadcastSystemMessage(interviewId, SystemEventType.JOIN, "사용자가 입장했습니다.");
    }

    // 사용자 퇴장
    public void handleUserLeave(String sessionId) {
        Long interviewId = sessionRegistry.getInterviewIdBySession(sessionId);
        if(interviewId == null) return;

        sessionRegistry.removeSession(sessionId);
        log.info("LEAVE: sessionId={} interviewId={}", sessionId, interviewId);

        broadcastSystemMessage(interviewId, SystemEventType.LEAVE, "사용자가 퇴장했습니다.");

        if(sessionRegistry.getSessionCount(interviewId) == 0) {
            log.info("INTERVIEW END: interviewId={} 모든 사용자가 퇴장했습니다.", interviewId);
            interviewService.finishInterview(interviewId);
        }
    }

    public void broadcastSystemMessage(Long interviewId, SystemEventType event, String content) {
        SystemMessage outgoing = new SystemMessage(interviewId, event, content);

        messagingTemplate.convertAndSend(
                topic(interviewId, "system"),
                outgoing
        );

        log.info("[SYSTEM] interviewId={} event={} content={}", interviewId, event, content);
    }


    public void broadcastChatMessage(Long interviewId, ChatMessage message) {

        // DB 저장
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .interviewId(interviewId)
                .senderId(message.getSenderId())
                .sender(message.getSender())
                .content(message.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(entity);


        // 메시지 전송
        ChatMessage outgoing = new ChatMessage(
                interviewId,
                message.getSenderId(),
                message.getSender(),
                message.getContent()
        );

        messagingTemplate.convertAndSend(
                topic(interviewId, "chat"),
                outgoing
        );

        log.info("[CHAT] interviewId={} senderId={} sender={} content={}",
                interviewId, message.getSenderId(), message.getSender(), message.getContent());
    }

    public void broadcastNoteMessage(Long interviewId, String sessionId, NoteMessageRequestDto noteMessage) {
        if (!sessionRegistry.isInterviewer(sessionId)) {
            log.warn("NOTE MESSAGE 차단됨 - sessionId={} 는 면접관이 아님", sessionId);
            return;
        }

        Long senderId = sessionRegistry.getUserIdBySession(sessionId);
        if (senderId == null || senderId == -1L) {
            log.warn("NOTE MESSAGE 차단됨 - sessionId={} 에 매핑된 userId가 없습니다.", sessionId);
            return;
        }

        String senderName = userService.getMyProfile(senderId).getName();

        InterviewNoteMemoCreateRequestDto requestDto =
                new InterviewNoteMemoCreateRequestDto(noteMessage.content());

        // DB 저장
        var response = interviewNoteMemoService.create(interviewId, requestDto);

        Long noteId = response.memoId();

        // 메시지 전송
        NoteMessage outgoing = NoteMessage.builder()
                .interviewId(interviewId)
                .senderId(senderId)
                .sender(senderName)
                .content(noteMessage.content())
                .noteId(noteId)
                .build();


        messagingTemplate.convertAndSend(
                topic(interviewId, "note"),
                outgoing
        );

        log.info("[NOTE] interviewId={} senderId={} sender={} noteId={}",
                interviewId, senderId, senderName, noteId);
    }

    // 토픽 경로 생성
    private String topic(Long interviewId, String type) {
        return "/topic/interview/" + interviewId + "/" + type;
    }
}
