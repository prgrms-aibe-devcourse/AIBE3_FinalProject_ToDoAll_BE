package com.server.dashboard.dto;

import com.server.dashboard.type.CalendarEventType;

import java.time.LocalDateTime;

public record DashboardCalendarEventDto(
        Long id,
        String time,              // "오전 10:00" or "10:00"
        String title,
        CalendarEventType type
) {

}
