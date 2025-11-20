package com.server.interview.websocket.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    // interviewId -> sessionId 목록
    private final Map<Long, Set<String>> interviewSessions = new ConcurrentHashMap<>();

    // sessionId -> interviewId
    private final Map<String, Long> sessionInterviewMap = new ConcurrentHashMap<>();

    // sessionId -> 면접관인지 여부
    private final Map<String, Boolean> sessionInterviewrMap = new ConcurrentHashMap<>();

    public void addSession(Long interviewId, String sessionId, boolean isInterviewer) {
        interviewSessions.computeIfAbsent(interviewId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        sessionInterviewMap.put(sessionId, interviewId);
        sessionInterviewrMap.put(sessionId, isInterviewer);
    }

    public void removeSession(String sessionId) {
        Long interviewId = sessionInterviewMap.get(sessionId);
        if(interviewId == null) return;

        interviewSessions.computeIfPresent(interviewId, (id, sessions) -> {
            sessions.remove(sessionId);
            return sessions.isEmpty() ? null : sessions; // 비면 map에서 제거
        });

        sessionInterviewMap.remove(sessionId);
        sessionInterviewrMap.remove(sessionId);
    }

    public int getSessionCount(Long interviewId) {
        return interviewSessions.getOrDefault(interviewId, Collections.emptySet()).size();
    }

    public Long getInterviewIdBySession(String sessionId) {
        return sessionInterviewMap.get(sessionId);
    }

    public boolean isInterviewer(String sessionId) {
        return Boolean.TRUE.equals(sessionInterviewrMap.get(sessionId));
    }
}
