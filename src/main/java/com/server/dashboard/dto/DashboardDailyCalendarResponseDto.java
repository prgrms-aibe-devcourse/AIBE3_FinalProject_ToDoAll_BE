package com.server.dashboard.dto;

import com.server.dashboard.type.CustomDayOfWeek;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.server.dashboard.util.Formatter.formatterDate;

public record DashboardDailyCalendarResponseDto(
        String date,
        CustomDayOfWeek dayOfWeek,
        List<DashboardCalendarEventResponseDto> events
) {
    public DashboardDailyCalendarResponseDto(LocalDate date) {
        this(date.format(formatterDate), CustomDayOfWeek.from(date.getDayOfWeek()), new ArrayList<>());
    }
    public void addCalendarEvents(Collection<DashboardCalendarEventResponseDto> calendarEventDtos) {
        events.addAll(calendarEventDtos);
    }

    public void addCalendarEvents(DashboardCalendarEventResponseDto calendarEventDto) {
        addCalendarEvents(List.of(calendarEventDto));
    }
}