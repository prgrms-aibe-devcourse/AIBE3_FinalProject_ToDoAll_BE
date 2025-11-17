package com.server.dashboard.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {
    static public DateTimeFormatter formatterDateDividedSlash = DateTimeFormatter.ofPattern("MM/dd");
    static public DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");
    static public DateTimeFormatter formatterDate= DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static public DateTimeFormatter formatterTimeWithAMPM = DateTimeFormatter.ofPattern("a HH:mm", Locale.KOREA);
}

