package com.server.interview.websocket;

import com.server.interview.websocket.service.SessionRegistry;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionRegistryConcurrencyTest {

    private final SessionRegistry registry = new SessionRegistry();

    private static final int THREADS = 100;
    private static final int OPERATIONS = 10_000;

    @Test
    void concurrentAddSessionTest() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        long interviewId = ThreadLocalRandom.current().nextInt(1, 100);

        long start = System.currentTimeMillis(); // ⬅ 시간 측정 시작

        for (int i = 0; i < OPERATIONS; i++) {
            executor.submit(() -> {
                String sessionId = UUID.randomUUID().toString();
                registry.addSession(interviewId, sessionId, false);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long end = System.currentTimeMillis(); // ⬅ 시간 측정 끝

        int count = registry.getSessionCount(interviewId);
        System.out.println("최종 세션 수 = " + count);
        System.out.println("걸린 시간 = " + (end - start) + " ms");

        assertThat(count).isEqualTo(OPERATIONS);
    }

    @Test
    void concurrentAddAndRemoveSessionTest() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        long interviewId = ThreadLocalRandom.current().nextInt(1, 100);

        ConcurrentLinkedQueue<String> sessionIds = new ConcurrentLinkedQueue<>();

        long start = System.currentTimeMillis(); // ⬅ 시간 측정 시작

        // add
        for (int i = 0; i < OPERATIONS; i++) {
            executor.submit(() -> {
                String sessionId = UUID.randomUUID().toString();
                sessionIds.add(sessionId);
                registry.addSession(interviewId, sessionId, false);
            });
        }

        // remove
        for (int i = 0; i < OPERATIONS; i++) {
            executor.submit(() -> {
                String sessionId = sessionIds.poll();
                if (sessionId != null) {
                    registry.removeSession(sessionId);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long end = System.currentTimeMillis(); // ⬅ 시간 측정 끝

        int count = registry.getSessionCount(interviewId);
        System.out.println("최종 세션 수 = " + count);
        System.out.println("걸린 시간 = " + (end - start) + " ms");

        assertThat(count).isEqualTo(0);
    }
}
