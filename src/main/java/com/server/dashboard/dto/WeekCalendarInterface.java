package com.server.dashboard.dto;

import com.server.dashboard.type.CalendarEventType;

import java.time.LocalDateTime;

public interface WeekCalendarInterface {
    Long getId();
    String getTitle();
    LocalDateTime getTime();
    CalendarEventType getType();
}
