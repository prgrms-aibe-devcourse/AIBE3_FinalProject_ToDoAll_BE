package com.server.interview.websocket.service;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SessionRegistry {

    // interviewId -> sessionId 목록
    private final Map<Long, Set<String>> interviewSessions = new HashMap<>();

    // sessionId -> interviewId
    private final Map<String, Long> sessionIntervieMap = new HashMap<>();

    public void addSession(Long interviewId, String sessionId) {
        interviewSessions.computeIfAbsent(interviewId, k -> new HashSet<>())
                .add(sessionId);
    }

    public void removeSession(String sessionId) {
        Long interviewId = sessionIntervieMap.get(sessionId);
        if(interviewId == null) return;

        Set<String> sessions = interviewSessions.get(interviewId);
        if(sessions != null) {
            sessions.remove(sessionId);
            if(sessions.isEmpty()) {
                interviewSessions.remove(interviewId);
            }
        }

        sessionIntervieMap.remove(sessionId);
    }

    public int getSessionCount(Long interviewId) {
        return interviewSessions.getOrDefault(interviewId, Collections.emptySet()).size();
    }

    public Long getInterviewIdBySession(String sessionId) {
        return sessionIntervieMap.get(sessionId);
    }
}
