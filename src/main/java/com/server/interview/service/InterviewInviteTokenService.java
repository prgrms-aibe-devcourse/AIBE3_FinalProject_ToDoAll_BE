package com.server.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewInviteTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.email.auth-expiry-minutes:30}")
    private long expiryMinutes;

    private static final String KEY_PREFIX = "interview:invite:";

    public String createToken(Long interviewId, Long resumeId, String email) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = KEY_PREFIX + token;

        String value = interviewId + "|" + resumeId + "|" + email;

        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(expiryMinutes));
        return token;
    }

    public Optional<InvitePayload> validateAndGet(String token, Long expectedInterviewId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + token);
        if(value == null) return Optional.empty();

        String[] parts = value.split("\\|", 3);
        if(parts.length != 3) return Optional.empty();

        Long interviewId = Long.valueOf(parts[0]);
        Long resumeId = Long.valueOf(parts[1]);
        String email = parts[2];

        if(!interviewId.equals(expectedInterviewId)) return Optional.empty();
        return Optional.of(new InvitePayload(interviewId,resumeId, email));
    }

    public record InvitePayload(Long interviewId, Long resumeId, String email) {}
}
