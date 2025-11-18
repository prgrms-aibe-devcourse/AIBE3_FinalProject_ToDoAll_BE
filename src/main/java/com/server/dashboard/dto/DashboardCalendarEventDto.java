package com.server.dashboard.dto;

import com.server.dashboard.type.CalendarEventType;

import java.time.LocalDateTime;

import static com.server.dashboard.util.Formatter.formatterTimeWithAMPM;

public record DashboardCalendarEventDto(
        String time,              // "오전 10:00" or "10:00"
        CalendarEventType type,
        int count
) {
    public DashboardCalendarEventDto(LocalDateTime time, CalendarEventType type, int count) {
        this(time.format(formatterTimeWithAMPM),  type, count);
    }
}
