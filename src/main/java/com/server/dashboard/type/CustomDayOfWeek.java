package com.server.dashboard.type;

import java.time.DayOfWeek;

public enum CustomDayOfWeek {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    public static CustomDayOfWeek from(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> MON;
            case TUESDAY -> TUE;
            case WEDNESDAY -> WED;
            case THURSDAY -> THU;
            case FRIDAY -> FRI;
            case SATURDAY -> SAT;
            case SUNDAY -> SUN;
        };
    }
}
