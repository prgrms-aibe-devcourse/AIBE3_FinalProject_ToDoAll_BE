package com.server.interview.websocket.registry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRegistryTest {

    @Test
    void removeSession_마지막세션제거시_interviewSessions_정리됨() {
        SessionRegistry registry = new SessionRegistry();

        Long interviewId = 1L;
        registry.addSession(interviewId, 10L, "s1", true);

        registry.removeSession("s1");

        assertThat(registry.getSessionCount(interviewId)).isEqualTo(0);
        assertThat(registry.getInterviewIdBySession("s1")).isNull();
    }

    @Test
    void 익명유저는_중복로그인_제거로직_적용안됨() {
        SessionRegistry registry = new SessionRegistry();

        Long interviewId = 1L;
        registry.addSession(interviewId, -1L, "a1", false);
        registry.addSession(interviewId, -1L, "a2", false);

        assertThat(registry.getSessionCount(interviewId)).isEqualTo(2);
    }

    @Test
    void isInterviewer_getUserIdBySession_동작() {
        SessionRegistry registry = new SessionRegistry();

        registry.addSession(1L, 10L, "s1", true);
        registry.addSession(1L, 11L, "s2", false);

        assertThat(registry.isInterviewer("s1")).isTrue();
        assertThat(registry.isInterviewer("s2")).isFalse();
        assertThat(registry.getUserIdBySession("s1")).isEqualTo(10L);
        assertThat(registry.getUserIdBySession("none")).isEqualTo(-1L);
    }
}
