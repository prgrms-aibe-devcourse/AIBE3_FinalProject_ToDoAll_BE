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

    // sessionId -> userId
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    // interviewId -> lock 객체
    private final Map<Long, Object> interviewLocks = new ConcurrentHashMap<>();

    private Object getLock(Long interviewId) {
        return interviewLocks.computeIfAbsent(interviewId, id -> new Object());
    }

//    protected void validateInterviewId(Long interviewId) {
//        // if (!interviewService.existsById(interviewId)) {
//        //     throw new IllegalArgumentException("Invalid interviewId: " + interviewId);
//        // }
//    }

    public void addSession(Long interviewId, Long userId, String sessionId, boolean isInterviewer) {
       //validateInterviewId(interviewId);
        Object lock = getLock(interviewId);

        synchronized (lock) {
            String existing = findSessionByUser(interviewId, userId);
            if(existing != null && !existing.equals(sessionId)) {
                removeSessionInternal(existing);
            }

            interviewSessions.computeIfAbsent(interviewId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

            sessionInterviewMap.put(sessionId, interviewId);
            sessionInterviewrMap.put(sessionId, isInterviewer);
            sessionUserMap.put(sessionId, userId);
        }
    }

    private String findSessionByUser(Long interviewId, Long userId) {
        return interviewSessions.getOrDefault(interviewId, Set.of()).stream()
                .filter(s -> userId.equals(sessionUserMap.get(s)))
                .findFirst()
                .orElse(null);
    }


    public void removeSession(String sessionId) {
        Long interviewId = sessionInterviewMap.get(sessionId);
        if(interviewId == null) return;

        Object lock = getLock(interviewId);

        synchronized (lock) {
            removeSessionInternal(sessionId);

            if (!interviewSessions.containsKey(interviewId)) {
                interviewLocks.remove(interviewId);
            }
        }
    }

    private void removeSessionInternal(String sessionId) {
        Long mappedInterviewId = sessionInterviewMap.remove(sessionId);
        if (mappedInterviewId == null) {
            return;
        }

        sessionInterviewrMap.remove(sessionId);
        sessionUserMap.remove(sessionId);

        interviewSessions.computeIfPresent(mappedInterviewId, (id, set) -> {
            set.remove(sessionId);
            return set.isEmpty() ? null : set;
        });
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

    public void removeSessionsByUserId(Long userId) {
        List<String> sessions = sessionUserMap.entrySet().stream()
                .filter(e -> e.getValue().equals(userId))
                .map(Map.Entry::getKey)
                .toList();

        sessions.forEach(this::removeSession);
    }

    public boolean isInterviewFinished(Long interviewId) {
        return getSessionCount(interviewId) == 0;
    }
}
