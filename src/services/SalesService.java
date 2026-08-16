package services;

import models.Booking;
import models.Hall;
import models.enums.SalesPeriod;
import repository.BookingRepository;
import repository.HallRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Revenue reporting for the Manager's sales dashboard.
 *
 * Only live bookings count towards revenue; cancelled ones are excluded. Rows
 * come back in chronological order because the grouping keys are zero-padded and
 * held in a {@link TreeMap}.
 */
public class SalesService {

    /** One line of the sales report. */
    public static class SalesRow {
        private final String period;
        private final int bookingCount;
        private final double revenue;

        SalesRow(String period, int bookingCount, double revenue) {
            this.period = period;
            this.bookingCount = bookingCount;
            this.revenue = revenue;
        }

        public String getPeriod() {
            return period;
        }

        public int getBookingCount() {
            return bookingCount;
        }

        public double getRevenue() {
            return revenue;
        }

        public double getAveragePerBooking() {
            return bookingCount == 0 ? 0d : revenue / bookingCount;
        }
    }

    private static final DateTimeFormatter MONTH_LABEL =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL =
            DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final BookingRepository bookings = new BookingRepository();
    private final HallRepository halls = new HallRepository();

    /**
     * Groups revenue weekly, monthly or yearly.
     *
     * @param from earliest booking date to include, or null for no lower bound
     * @param to   latest booking date to include, or null for no upper bound
     */
    public List<SalesRow> summarise(SalesPeriod period, LocalDate from, LocalDate to) {
        Map<String, int[]> counts = new TreeMap<>();
        Map<String, double[]> totals = new HashMap<>();
        Map<String, String> labels = new HashMap<>();

        for (Booking booking : inRange(from, to)) {
            LocalDate day = booking.getStart().toLocalDate();
            String key = keyFor(period, day);
            labels.putIfAbsent(key, labelFor(period, day));
            counts.computeIfAbsent(key, k -> new int[1])[0]++;
            totals.computeIfAbsent(key, k -> new double[1])[0] += booking.getTotalCost();
        }

        List<SalesRow> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            String key = entry.getKey();
            rows.add(new SalesRow(labels.get(key), entry.getValue()[0], totals.get(key)[0]));
        }
        return rows;
    }

    /** Revenue per hall over the same range, as a secondary breakdown. */
    public List<SalesRow> summariseByHall(LocalDate from, LocalDate to) {
        Map<String, String> hallNames = new HashMap<>();
        for (Hall hall : halls.findAll()) {
            hallNames.put(hall.getId(), hall.getName());
        }

        Map<String, int[]> counts = new TreeMap<>();
        Map<String, double[]> totals = new HashMap<>();
        for (Booking booking : inRange(from, to)) {
            String key = booking.getHallId();
            counts.computeIfAbsent(key, k -> new int[1])[0]++;
            totals.computeIfAbsent(key, k -> new double[1])[0] += booking.getTotalCost();
        }

        List<SalesRow> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            String hallId = entry.getKey();
            String label = hallNames.getOrDefault(hallId, "Unknown hall") + " (" + hallId + ")";
            rows.add(new SalesRow(label, entry.getValue()[0], totals.get(hallId)[0]));
        }
        return rows;
    }

    public double totalRevenue(LocalDate from, LocalDate to) {
        double total = 0d;
        for (Booking booking : inRange(from, to)) {
            total += booking.getTotalCost();
        }
        return total;
    }

    public int totalBookings(LocalDate from, LocalDate to) {
        return inRange(from, to).size();
    }

    // ---------- internals ----------

    /** Live bookings whose start date falls inside the range. */
    private List<Booking> inRange(LocalDate from, LocalDate to) {
        List<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings.findAll()) {
            if (!booking.isActive()) {
                continue; // cancelled bookings are not revenue
            }
            LocalDate day = booking.getStart().toLocalDate();
            if (from != null && day.isBefore(from)) {
                continue;
            }
            if (to != null && day.isAfter(to)) {
                continue;
            }
            matches.add(booking);
        }
        return matches;
    }

    private static String keyFor(SalesPeriod period, LocalDate day) {
        switch (period) {
            case WEEKLY:
                return String.format("%04d-W%02d",
                        day.get(WeekFields.ISO.weekBasedYear()),
                        day.get(WeekFields.ISO.weekOfWeekBasedYear()));
            case YEARLY:
                return String.format("%04d", day.getYear());
            case MONTHLY:
            default:
                return String.format("%04d-%02d", day.getYear(), day.getMonthValue());
        }
    }

    private static String labelFor(SalesPeriod period, LocalDate day) {
        switch (period) {
            case WEEKLY: {
                LocalDate monday = day.with(WeekFields.ISO.dayOfWeek(), 1);
                LocalDate sunday = monday.plusDays(6);
                return "Week " + day.get(WeekFields.ISO.weekOfWeekBasedYear())
                        + ", " + day.get(WeekFields.ISO.weekBasedYear())
                        + " (" + monday.format(DAY_LABEL) + " - " + sunday.format(DAY_LABEL) + ")";
            }
            case YEARLY:
                return String.valueOf(day.getYear());
            case MONTHLY:
            default:
                return YearMonth.from(day).format(MONTH_LABEL);
        }
    }
}
