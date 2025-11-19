package com.server.interview.websocket.service;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SessionRegistry {

    // interviewId -> sessionId 목록
    private final Map<Long, Set<String>> interviewSessions = new HashMap<>();

    // sessionId -> interviewId
    private final Map<String, Long> sessionInterviewMap = new HashMap<>();

    // sessionId -> 면접관인지 여부
    private final Map<String, Boolean> sessionInterviewrMap = new HashMap<>();

    public void addSession(Long interviewId, String sessionId, boolean isInterviewer) {
        interviewSessions.computeIfAbsent(interviewId, k -> new HashSet<>())
                .add(sessionId);

        sessionInterviewMap.put(sessionId, interviewId);
        sessionInterviewrMap.put(sessionId, isInterviewer);
    }

    public void removeSession(String sessionId) {
        Long interviewId = sessionInterviewMap.get(sessionId);
        if(interviewId == null) return;

        Set<String> sessions = interviewSessions.get(interviewId);
        if(sessions != null) {
            sessions.remove(sessionId);
            if(sessions.isEmpty()) {
                interviewSessions.remove(interviewId);
            }
        }

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
