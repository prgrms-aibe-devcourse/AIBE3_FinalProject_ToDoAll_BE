package com.server.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.global.exception.ApplicationException;
import com.server.interview.exception.InterviewInviteTokenErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewInviteTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.email.auth-expiry-minutes:30}")
    private long expiryMinutes;

    private static final String KEY_PREFIX = "interview:invite:";
    private static final Duration DEFAULT_DELETE_LOCK = Duration.ofSeconds(3);

    public String createToken(Long interviewId, Long resumeId, String email) {
        if (interviewId == null || resumeId == null || email == null || email.isBlank()) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_PAYLOAD_INVALID);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String key = KEY_PREFIX + token;

        InvitePayload payload = new InvitePayload(interviewId, resumeId, email);

        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(expiryMinutes));
            return token;
        } catch (JsonProcessingException e) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_SERIALIZE_FAILED);
        }
    }


    public InvitePayload validateAndGetOrThrow(String token, Long expectedInterviewId) {
        validateTokenFormat(token);
        if (expectedInterviewId == null) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_PAYLOAD_INVALID);
        }

        String key = KEY_PREFIX + token;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_NOT_FOUND);
        }

        final InvitePayload payload;
        try {
            payload = objectMapper.readValue(json, InvitePayload.class);
        } catch (Exception e) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_PAYLOAD_INVALID);
        }

        if (payload.interviewId() == null || payload.resumeId() == null || payload.email() == null) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_PAYLOAD_INVALID);
        }

        if (!payload.interviewId().equals(expectedInterviewId)) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_MISMATCH);
        }

        return payload;
    }

    public void deleteTokenOrThrow(String token) {
        validateTokenFormat(token);

        String key = KEY_PREFIX + token;
        try {
            Boolean deleted = redisTemplate.delete(key);

            if (deleted == null || !deleted) {
                throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_NOT_FOUND);

            }
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVITE_TOKEN_DELETE_FAILED);
        }
    }


    public InvitePayload validateAndConsumeOrThrow(String token, Long expectedInterviewId) {
        InvitePayload payload = validateAndGetOrThrow(token, expectedInterviewId);
        deleteTokenOrThrow(token);
        return payload;
    }

    private void validateTokenFormat(String token) {
        if (token == null || token.isBlank()) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVALID_TOKEN);
        }

        if (token.length() != 32) {
            throw new ApplicationException(InterviewInviteTokenErrorCase.INVALID_TOKEN);
        }
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            boolean isHex = (c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'f') ||
                    (c >= 'A' && c <= 'F');
            if (!isHex) {
                throw new ApplicationException(InterviewInviteTokenErrorCase.INVALID_TOKEN);
            }
        }
    }

    public record InvitePayload(Long interviewId, Long resumeId, String email) {}
}
