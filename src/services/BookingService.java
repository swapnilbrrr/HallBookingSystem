package services;

import models.Availability;
import models.Booking;
import models.Hall;
import models.Maintenance;
import models.enums.BookingStatus;
import models.enums.Timeframe;
import repository.AvailabilityRepository;
import repository.BookingRepository;
import repository.HallRepository;
import repository.MaintenanceRepository;
import utils.DateUtil;
import utils.Validator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Booking, cancellation, and the search for bookable halls.
 *
 * This is where the rules from the brief are enforced. A slot is only sold when
 * all four hold:
 * <ol>
 *   <li>it lies inside the 08:00-18:00 trading day;</li>
 *   <li>a scheduler has opened an availability window that covers it;</li>
 *   <li>no maintenance window overlaps it;</li>
 *   <li>no live booking for that hall overlaps it.</li>
 * </ol>
 * Cancellation additionally requires three clear days' notice.
 */
public class BookingService {

    /** The trading day is ten hours long, so no single booking can exceed that. */
    private static final long MAX_BOOKING_HOURS =
            Duration.between(DateUtil.OPENING_TIME, DateUtil.CLOSING_TIME).toHours();

    private final BookingRepository bookings = new BookingRepository();
    private final HallRepository halls = new HallRepository();
    private final AvailabilityRepository availability = new AvailabilityRepository();
    private final MaintenanceRepository maintenance = new MaintenanceRepository();

    // ---------- browsing halls ----------

    /** Every hall still on offer, for browsing before a date is chosen. */
    public List<Hall> findBookableHalls() {
        return halls.findActive();
    }

    /**
     * Halls that can actually be booked for the given slot: on offer, opened by a
     * scheduler, not under maintenance, and not already taken.
     */
    public List<Hall> findHallsAvailableFor(LocalDateTime start, LocalDateTime end) {
        List<Hall> free = new ArrayList<>();
        for (Hall hall : halls.findActive()) {
            if (isFree(hall.getId(), start, end)) {
                free.add(hall);
            }
        }
        return free;
    }

    private boolean isFree(String hallId, LocalDateTime start, LocalDateTime end) {
        return isWithinAvailability(hallId, start, end)
                && findMaintenanceClash(hallId, start, end) == null
                && findBookingClash(hallId, start, end) == null;
    }

    /** Plain-language reason a hall cannot take the slot, or null when it can. */
    public String explainUnavailability(String hallId, LocalDateTime start, LocalDateTime end) {
        if (!isWithinAvailability(hallId, start, end)) {
            return "The scheduler has not opened this hall for that period.";
        }
        Maintenance underRepair = findMaintenanceClash(hallId, start, end);
        if (underRepair != null) {
            return "Closed for maintenance (" + underRepair.getDisplayRange() + ").";
        }
        Booking taken = findBookingClash(hallId, start, end);
        if (taken != null) {
            return "Already booked (" + taken.getDisplayRange() + ").";
        }
        return null;
    }

    // ---------- creating a booking ----------

    /**
     * Sells a slot and returns the stored booking.
     *
     * Date, time and duration arrive as raw text from the payment form so that all
     * parsing and validation happen in one place.
     *
     * @throws Validator.ValidationException with a message fit to show the user
     */
    public Booking create(String customerId, String hallId, String dateText,
                          String startTimeText, String hoursText) {
        Hall hall = halls.findById(hallId)
                .orElseThrow(() -> new Validator.ValidationException(
                        "That hall no longer exists. Please choose another."));
        if (!hall.isActive()) {
            Validator.fail(hall.getName() + " is no longer available for booking.");
        }

        LocalDate date = DateUtil.parseInputDate(dateText, "Booking date");
        LocalTime time = DateUtil.parseInputTime(startTimeText, "Start time");
        int hours = Validator.intInRange(hoursText, "Hours to book", 1, (int) MAX_BOOKING_HOURS);

        LocalDateTime start = LocalDateTime.of(date, time);
        LocalDateTime end = start.plusHours(hours);

        if (!start.isAfter(LocalDateTime.now())) {
            Validator.fail("The booking must start in the future.");
        }
        if (!DateUtil.withinOperatingHours(start, end)) {
            Validator.fail("Bookings must fall within opening hours ("
                    + DateUtil.operatingHoursText() + ") on a single day."
                    + " A " + hours + "-hour booking from " + DateUtil.displayTime(start)
                    + " would end at " + DateUtil.displayTime(end) + ".");
        }

        String reason = explainUnavailability(hallId, start, end);
        if (reason != null) {
            Validator.fail(hall.getName() + " cannot be booked for that slot. " + reason);
        }

        Booking booking = new Booking(
                bookings.nextId("B"),
                customerId,
                hallId,
                start,
                end,
                hall.costFor(hours),
                BookingStatus.PAID,
                LocalDateTime.now());
        bookings.insert(booking);
        return booking;
    }

    // ---------- cancelling ----------

    /**
     * Cancels a booking, provided it is at least three days away.
     *
     * The notice period is computed from the stored timestamp, so there is no
     * parsing step that could fail and let an ineligible cancellation through.
     */
    public void cancel(Booking booking) {
        if (!booking.isActive()) {
            Validator.fail("Booking " + booking.getId() + " has already been cancelled.");
        }
        if (booking.isPast()) {
            Validator.fail("Booking " + booking.getId()
                    + " has already taken place, so it cannot be cancelled.");
        }
        long daysAway = DateUtil.daysUntil(booking.getStart());
        if (daysAway < DateUtil.MIN_CANCELLATION_DAYS) {
            Validator.fail("Bookings may only be cancelled at least "
                    + DateUtil.MIN_CANCELLATION_DAYS + " days before the booking date."
                    + " This booking is " + daysAway + " day(s) away.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookings.update(booking);
    }

    // ---------- queries (overloaded for each screen) ----------

    /** Every booking in the system, for the Administrator's overview. */
    public List<Booking> findBookings() {
        return bookings.findAll();
    }

    /** One customer's bookings. */
    public List<Booking> findBookings(String customerId) {
        return bookings.findByCustomer(customerId);
    }

    /** All bookings starting within an inclusive date range. */
    public List<Booking> findBookings(LocalDate from, LocalDate to) {
        List<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings.findAll()) {
            LocalDate day = booking.getStart().toLocalDate();
            if (!day.isBefore(from) && !day.isAfter(to)) {
                matches.add(booking);
            }
        }
        return matches;
    }

    public Optional<Booking> findById(String id) {
        return bookings.findById(id);
    }

    /**
     * The one filter both booking tables use.
     *
     * A null customerId spans every customer, a null status accepts both, and
     * blank text matches everything.
     */
    public List<Booking> filter(String customerId, Timeframe timeframe, BookingStatus status,
                                String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        Map<String, String> hallNames = hallNamesById();

        List<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings.findAll()) {
            if (customerId != null && !booking.getCustomerId().equals(customerId)) {
                continue;
            }
            if (status != null && booking.getStatus() != status) {
                continue;
            }
            if (timeframe == Timeframe.UPCOMING && booking.isPast()) {
                continue;
            }
            if (timeframe == Timeframe.PAST && !booking.isPast()) {
                continue;
            }
            if (!needle.isEmpty()) {
                String hallName = hallNames.getOrDefault(booking.getHallId(), "");
                boolean hit = booking.getId().toLowerCase().contains(needle)
                        || booking.getHallId().toLowerCase().contains(needle)
                        || booking.getCustomerId().toLowerCase().contains(needle)
                        || hallName.toLowerCase().contains(needle)
                        || booking.getDisplayRange().toLowerCase().contains(needle)
                        || booking.getStatus().getLabel().toLowerCase().contains(needle);
                if (!hit) {
                    continue;
                }
            }
            matches.add(booking);
        }
        return matches;
    }

    /** Hall id to hall name, so booking tables can show something readable. */
    public Map<String, String> hallNamesById() {
        Map<String, String> names = new HashMap<>();
        for (Hall hall : halls.findAll()) {
            names.put(hall.getId(), hall.getName());
        }
        return names;
    }

    // ---------- rule checks ----------

    private boolean isWithinAvailability(String hallId, LocalDateTime start, LocalDateTime end) {
        for (Availability window : availability.findByHall(hallId)) {
            if (window.covers(start, end)) {
                return true;
            }
        }
        return false;
    }

    private Maintenance findMaintenanceClash(String hallId, LocalDateTime start,
                                             LocalDateTime end) {
        for (Maintenance window : maintenance.findByHall(hallId)) {
            if (window.overlaps(start, end)) {
                return window;
            }
        }
        return null;
    }

    private Booking findBookingClash(String hallId, LocalDateTime start, LocalDateTime end) {
        for (Booking booking : bookings.findActiveByHall(hallId)) {
            if (booking.overlaps(start, end)) {
                return booking;
            }
        }
        return null;
    }
}
