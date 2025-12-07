package com.server.dashboard.dto;

import com.server.dashboard.type.CustomDayOfWeek;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public record DashboardWeeklyCalendarResponseDto(
    LocalDate weekStart,
    LocalDate weekEnd,
    DashboardCalendarEventContainer dailyCalendars
) {

    public DashboardWeeklyCalendarResponseDto(LocalDate mon, LocalDate sun) {
        this(mon, sun, createCalendarEventContainer(mon));
    }

    private static DashboardCalendarEventContainer createCalendarEventContainer(LocalDate weekStart) {
        var days = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> new DashboardDailyCalendarDto(weekStart.plusDays(i)))
                .toList();

        return new DashboardCalendarEventContainer(
                days.get(0),
                days.get(1),
                days.get(2),
                days.get(3),
                days.get(4),
                days.get(5),
                days.get(6)
        );
    }

    public void addCalendarEvents(CustomDayOfWeek dayOfWeek, Collection<DashboardCalendarEventDto> calendarEventDtos) {
        dailyCalendars.add(dayOfWeek, calendarEventDtos);
    }

    public void addCalendarEvent(CustomDayOfWeek dayOfWeek, DashboardCalendarEventDto calendarEventDto) {
        addCalendarEvents(dayOfWeek, List.of(calendarEventDto));
    }

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
}
