package com.taskflow.approvalsystem.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private DateUtils() {

    }

    public static LocalDate parseOptionalDate(String dateStr) {
        return parseOptionalDate(dateStr, DEFAULT_FORMATTER);
    }

    public static LocalDate parseOptionalDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null) return null;
        String s = dateStr.trim();
        if (s.isEmpty()) return null;
        return LocalDate.parse(s, formatter);
    }
}

