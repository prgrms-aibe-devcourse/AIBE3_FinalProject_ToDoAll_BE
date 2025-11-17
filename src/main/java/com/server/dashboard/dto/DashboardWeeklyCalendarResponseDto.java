package com.server.dashboard.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record DashboardWeeklyCalendarResponseDto(
    LocalDate weekStart,
    LocalDate weekEnd,
    List<DashboardDailyCalendarResponseDto> dailyCalendars
) {
    public DashboardWeeklyCalendarResponseDto(LocalDate weekStart, LocalDate weekEnd) {
        this(weekStart, weekEnd, new ArrayList<>());
    }

    public void addDailyCalendars(Collection<DashboardDailyCalendarResponseDto> _dailyCalendars) {
        dailyCalendars.addAll(_dailyCalendars);
    }
    public void addDailyCalendars(DashboardDailyCalendarResponseDto dailyCalendarDto) {
        addDailyCalendars(List.of(dailyCalendarDto));
    }
}
