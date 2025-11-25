package com.server.interview.websocket.registry;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    // interviewId -> sessionId 목록
    private final Map<Long, Set<String>> interviewSessions = new ConcurrentHashMap<>();

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    // interviewId -> lock 객체
    private final Map<Long, Object> interviewLocks = new ConcurrentHashMap<>();

    private Object getLock(Long interviewId) {
        return interviewLocks.computeIfAbsent(interviewId, id -> new Object());
    }


    public void addSession(Long interviewId, Long userId, String sessionId, boolean isInterviewer) {

        if (userId == null) userId = -1L;

        Object lock = getLock(interviewId);

        synchronized (lock) {
            String existing = findSessionByUser(interviewId, userId);
            if(existing != null && !existing.equals(sessionId)) {
                removeSessionInternal(existing);
            }

            sessions.put(sessionId, new SessionInfo(interviewId, userId, isInterviewer));

            interviewSessions
                    .computeIfAbsent(interviewId, id -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
        }
    }

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


    public void removeSession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        if (info == null) return;

        Long interviewId = info.getInterviewId();
        Object lock = getLock(interviewId);

        synchronized (lock) {
            removeSessionInternal(sessionId);

            if (!interviewSessions.containsKey(interviewId)) {
                interviewLocks.remove(interviewId);
            }
        }
    }

    private void removeSessionInternal(String sessionId) {
        SessionInfo info = sessions.remove(sessionId);
        if (info == null) return;

        Long interviewId = info.getInterviewId();

        interviewSessions.computeIfPresent(interviewId, (id, set) -> {
            set.remove(sessionId);
            return set.isEmpty() ? null : set;
        });
    }

    public int getSessionCount(Long interviewId) {
        return interviewSessions.getOrDefault(interviewId, Collections.emptySet()).size();
    }

    public Long getInterviewIdBySession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.getInterviewId() : null;
    }

    public boolean isInterviewer(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null && info.isInterviewer();
    }

    public Long getUserIdBySession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.getUserId() : null;
    }

}
