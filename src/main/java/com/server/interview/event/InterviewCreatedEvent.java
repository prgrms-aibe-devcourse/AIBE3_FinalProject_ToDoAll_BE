package com.server.interview.event;

import java.time.LocalDateTime;

public record InterviewCreatedEvent(
        Long interviewId,
        LocalDateTime interviewAt
) {}
