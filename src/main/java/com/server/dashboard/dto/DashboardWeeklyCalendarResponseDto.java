package com.server.dashboard.dto;

import com.server.dashboard.type.CustomDayOfWeek;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

record DashboardCalendarEventContainer(
        DashboardDailyCalendarDto mon,
        DashboardDailyCalendarDto tue,
        DashboardDailyCalendarDto wed,
        DashboardDailyCalendarDto thu,
        DashboardDailyCalendarDto fri,
        DashboardDailyCalendarDto sat,
        DashboardDailyCalendarDto sun
        ) {
    public void add(CustomDayOfWeek customDayOfWeek, Collection<DashboardCalendarEventDto> _calendarEventDtos) {
        switch (customDayOfWeek) {
            case MON->mon.addCalendarEvents(_calendarEventDtos);
            case TUE->tue.addCalendarEvents(_calendarEventDtos);
            case WED->wed.addCalendarEvents(_calendarEventDtos);
            case THU->thu.addCalendarEvents(_calendarEventDtos);
            case FRI->fri.addCalendarEvents(_calendarEventDtos);
            case SAT->sat.addCalendarEvents(_calendarEventDtos);
            case SUN->sun.addCalendarEvents(_calendarEventDtos);
        }
    }
}


public record DashboardWeeklyCalendarResponseDto(
    LocalDate weekStart,
    LocalDate weekEnd,
    DashboardCalendarEventContainer dailyCalendars
) {

    public DashboardWeeklyCalendarResponseDto(LocalDate mon, LocalDate sun) {
        this(mon, sun, createCalendarEventContainer(mon));
    }

    private static DashboardCalendarEventContainer createCalendarEventContainer(LocalDate weekStart) {
        return new DashboardCalendarEventContainer(
            new DashboardDailyCalendarDto(weekStart),
            new DashboardDailyCalendarDto(weekStart.plusDays(1)),
            new DashboardDailyCalendarDto(weekStart.plusDays(2)),
            new DashboardDailyCalendarDto(weekStart.plusDays(3)),
            new DashboardDailyCalendarDto(weekStart.plusDays(4)),
            new DashboardDailyCalendarDto(weekStart.plusDays(5)),
            new DashboardDailyCalendarDto(weekStart.plusDays(6))
        );
    }

    public void addCalendarEvents(CustomDayOfWeek dayOfWeek, Collection<DashboardCalendarEventDto> calendarEventDtos) {
        dailyCalendars.add(dayOfWeek, calendarEventDtos);
    }

    public void addCalendarEvent(CustomDayOfWeek dayOfWeek, DashboardCalendarEventDto calendarEventDto) {
        addCalendarEvents(dayOfWeek, List.of(calendarEventDto));
    }
}
