package com.server.dashboard.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {
    public static final DateTimeFormatter formatterDateDividedSlash = DateTimeFormatter.ofPattern("MM/dd");
    public static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter formatterDate= DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter formatterTimeWithAMPM = DateTimeFormatter.ofPattern("a HH:mm", Locale.KOREA);
}

