package com.thaca.framework.core.utils;

import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.util.Strings;

public class DateUtils {

    private static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String DATE_ONLY_FORMAT = "dd-MM-yyyy";

    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_ONLY_FORMAT);

    private static ApiHeader getHeader() {
        return FwContextHeader.get();
    }

    public static String dateToString(Instant instant) {
        if (instant == null) return Strings.EMPTY;
        return dtf.format(instant.atZone(resolveZone()));
    }

    public static Instant stringToDate(String dateString) {
        if (Strings.isBlank(dateString)) return null;
        LocalDateTime ldt = LocalDateTime.parse(dateString, dtf);
        return ldt.atZone(resolveZone()).toInstant();
    }

    public static LocalDate stringToLocalDate(String dateString) {
        if (Strings.isBlank(dateString)) return null;
        return LocalDate.parse(dateString, df);
    }

    public static String localDateToString(LocalDate date) {
        if (date == null) return Strings.EMPTY;
        return df.format(date);
    }

    public static LocalDateTime stringToLocalDateTime(String dateString) {
        if (Strings.isBlank(dateString)) return null;
        return LocalDateTime.parse(dateString, dtf);
    }

    public static String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) return Strings.EMPTY;
        return dtf.format(dateTime);
    }

    public static String getCurrentDate() {
        return DateTimeFormatter.ofPattern("ddMMyyyy").format(Instant.now().atZone(resolveZone()));
    }

    public static Instant parseDateOnly(String dateString) {
        if (Strings.isBlank(dateString)) return null;
        LocalDate localDate = LocalDate.parse(dateString, df);
        return localDate.atStartOfDay(resolveZone()).toInstant();
    }

    private static ZoneId resolveZone() {
        try {
            ApiHeader header = getHeader();
            if (header == null || header.getLanguage() == null) {
                return ZoneId.of("Asia/Ho_Chi_Minh");
            }
            String lang = header.getLanguage().toLowerCase();
            if ("vi".equals(lang)) {
                return ZoneId.of("Asia/Ho_Chi_Minh");
            }
            if (header.getLocation() != null && !header.getLocation().isBlank()) {
                return ZoneId.of(header.getLocation());
            }
            return ZoneId.systemDefault();
        } catch (Exception e) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }
}
