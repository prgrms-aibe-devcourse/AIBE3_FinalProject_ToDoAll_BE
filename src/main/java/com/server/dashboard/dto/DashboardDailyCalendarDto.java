package com.server.dashboard.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.server.dashboard.util.Formatter.formatterDate;

record DashboardDailyCalendarDto(
        String date,
        List<DashboardCalendarEventDto> events
) {
    DashboardDailyCalendarDto(LocalDate date) {
        this(date.format(formatterDate), new ArrayList<>());
    }
    void addCalendarEvents(Collection<DashboardCalendarEventDto> calendarEventDtos) {
        events.addAll(calendarEventDtos);
    }
}