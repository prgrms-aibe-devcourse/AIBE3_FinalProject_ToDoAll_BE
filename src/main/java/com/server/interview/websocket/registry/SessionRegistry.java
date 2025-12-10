package com.server.interview.websocket.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionRegistry {

    // interviewId 번호별 현재 접속한 sessionId 목록
    private final Map<Long, Set<String>> interviewSessions = new ConcurrentHashMap<>();

    // sessionId별 유저 정보
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();


    // 세션 등록
    public void addSession(Long interviewId, Long userId, String sessionId, boolean isInterviewer) {
        log.info("REGISTRY addSession interviewId={} sessionId={} userId={} isInterviewer={}",
                interviewId, sessionId, userId, isInterviewer);

        Objects.requireNonNull(userId, "userId는 필수입니다.");

        sessions.put(sessionId, new SessionInfo(interviewId, userId, isInterviewer));

        interviewSessions.computeIfAbsent(interviewId, id -> ConcurrentHashMap.newKeySet());

        String existing = findSessionByUser(interviewId, userId);
        if (userId != -1L && existing != null && !existing.equals(sessionId)) {
            removeSessionInternal(existing);
        }

        interviewSessions.get(interviewId).add(sessionId);

    }

    // 현재 특정 유저가 로그인하고 있는지 확인
    private String findSessionByUser(Long interviewId, Long userId) {
        return interviewSessions
                .getOrDefault(interviewId, Set.of())
                .stream()
                .filter(sId -> {
                    SessionInfo info = sessions.get(sId);
                    return info != null && Objects.equals(info.getUserId(), userId);
                })
                .findFirst()
                .orElse(null);
    }


    // 세션 제거
    public void removeSession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        if (info == null) return;

        Long interviewId = info.getInterviewId();

        synchronized (interviewSessions.computeIfAbsent(interviewId, id -> ConcurrentHashMap.newKeySet())) {
            removeSessionInternal(sessionId);
        }
    }

    // 세션 깔끔하게 삭제
    private void removeSessionInternal(String sessionId) {
        SessionInfo info = sessions.remove(sessionId);
        if (info == null) return;

        Long interviewId = info.getInterviewId();

        interviewSessions.computeIfPresent(interviewId, (id, set) -> {
            set.remove(sessionId);
            return set.isEmpty() ? null : set;
        });
    }

    // 특정 interviewId에 몇 명이 접속했는지 확인
    public int getSessionCount(Long interviewId) {
        Set<String> set = interviewSessions.get(interviewId);
        return (set != null ? set.size() : 0);
    }

    // 이 세션이 어느 인터뷰 방인지 확인
    public Long getInterviewIdBySession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.getInterviewId() : null;
    }

    // 이 세션이 면접관인지 확인
    public boolean isInterviewer(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null && info.isInterviewer();
    }

    // 이 노트 작성자의 userId 확인
    public Long getUserIdBySession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.getUserId() : -1L;
    }

}
