package utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

/**
 * Single source of truth for date/time handling.
 *
 * Storage uses ISO-8601 (2026-09-01T08:00) so records sort naturally as text;
 * the user interface uses dd-MM-yyyy HH:mm. Keeping both formats here stops
 * ad-hoc patterns from drifting apart across screens.
 */
public final class DateUtil {

    /** Hall Symphony Inc. operates daily from 8:00 AM to 6:00 PM. */
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);

    /** A booking may only be cancelled at least this many days beforehand. */
    public static final int MIN_CANCELLATION_DAYS = 3;

    private static final DateTimeFormatter STORAGE = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Strict parser for typed dates.
     *
     * The default SMART resolver quietly turns 31-02-2026 into 28-02-2026; STRICT
     * rejects it instead, which is what input validation should do. STRICT requires
     * the proleptic year symbol 'uuuu' rather than 'yyyy'.
     */
    private static final DateTimeFormatter INPUT_DATE =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private DateUtil() {
    }

    // ---------- storage format ----------

    public static String toStorage(LocalDateTime value) {
        return value == null ? "" : value.format(STORAGE);
    }

    public static LocalDateTime fromStorage(String value) {
        return LocalDateTime.parse(value.trim(), STORAGE);
    }

    /** Lenient variant for optional stored fields; returns null when absent. */
    public static LocalDateTime fromStorageOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return fromStorage(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // ---------- display format ----------

    public static String display(LocalDateTime value) {
        return value == null ? "-" : value.format(DISPLAY_DATE_TIME);
    }

    public static String displayDate(LocalDateTime value) {
        return value == null ? "-" : value.format(DISPLAY_DATE);
    }

    public static String displayDate(LocalDate value) {
        return value == null ? "-" : value.format(DISPLAY_DATE);
    }

    public static String displayTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DISPLAY_TIME);
    }

    /** Renders a slot as "01-09-2026 08:00 - 12:00", collapsing the repeated date. */
    public static String displayRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "-";
        }
        if (start.toLocalDate().equals(end.toLocalDate())) {
            return display(start) + " - " + displayTime(end);
        }
        return display(start) + " - " + display(end);
    }

    // ---------- user input parsing ----------

    /** Parses a dd-MM-yyyy date, raising a message suitable for a dialog box. */
    public static LocalDate parseInputDate(String value, String fieldName) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new Validator.ValidationException(fieldName + " is required.");
        }
        try {
            return LocalDate.parse(text, INPUT_DATE);
        } catch (DateTimeParseException ex) {
            throw new Validator.ValidationException(
                    fieldName + " must be a real date in DD-MM-YYYY format (for example 25-12-2026).");
        }
    }

    /** Parses an HH:mm time, raising a message suitable for a dialog box. */
    public static LocalTime parseInputTime(String value, String fieldName) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new Validator.ValidationException(fieldName + " is required.");
        }
        try {
            return LocalTime.parse(text, DISPLAY_TIME);
        } catch (DateTimeParseException ex) {
            throw new Validator.ValidationException(
                    fieldName + " must be a 24-hour time in HH:MM format (for example 14:30).");
        }
    }

    public static LocalDateTime parseInputDateTime(String date, String time, String fieldName) {
        return LocalDateTime.of(parseInputDate(date, fieldName + " date"),
                parseInputTime(time, fieldName + " time"));
    }

    // ---------- business rules ----------

    /** True when the whole slot sits inside the 08:00-18:00 trading day. */
    public static boolean withinOperatingHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return false;
        }
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return false; // A booking may not span midnight
        }
        return !start.toLocalTime().isBefore(OPENING_TIME)
                && !end.toLocalTime().isAfter(CLOSING_TIME);
    }

    public static String operatingHoursText() {
        return OPENING_TIME.format(DISPLAY_TIME) + " - " + CLOSING_TIME.format(DISPLAY_TIME);
    }

    /** Whole hours between two instants; used for cost calculation and receipts. */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toHours();
    }

    public static long daysUntil(LocalDateTime target) {
        return ChronoUnit.DAYS.between(LocalDate.now(), target.toLocalDate());
    }

    public static boolean isCancellable(LocalDateTime bookingStart) {
        return daysUntil(bookingStart) >= MIN_CANCELLATION_DAYS;
    }

    public static boolean isPast(LocalDateTime value) {
        return value != null && value.isBefore(LocalDateTime.now());
    }
}
