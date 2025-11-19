package com.server.notification.dto;

public record NotificationDto(
        String type,
        String message,
        Long interviewId
) {}