package services;

import models.Booking;
import models.Hall;
import models.enums.HallStatus;
import models.enums.HallType;
import repository.BookingRepository;
import repository.HallRepository;
import utils.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Hall records: the Scheduler's "Hall Management" screen. */
public class HallService {

    /** Guard rail on the capacity field so a typo cannot create a 100000-seat room. */
    private static final int MAX_CAPACITY = 5000;

    private final HallRepository halls = new HallRepository();
    private final BookingRepository bookings = new BookingRepository();

    // ---------- queries ----------

    public List<Hall> findAll() {
        return halls.findAll();
    }

    /** Only halls still on offer; this is what customers may book. */
    public List<Hall> findActive() {
        return halls.findActive();
    }

    public Optional<Hall> findById(String id) {
        return halls.findById(id);
    }

    /**
     * Filters halls by free text, type, minimum capacity and status.
     * Null type or status means "any"; blank text and zero capacity match everything.
     */
    public List<Hall> filter(String query, HallType type, int minCapacity, HallStatus status) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        List<Hall> matches = new ArrayList<>();
        for (Hall hall : halls.findAll()) {
            if (type != null && hall.getType() != type) {
                continue;
            }
            if (status != null && hall.getStatus() != status) {
                continue;
            }
            if (hall.getCapacity() < minCapacity) {
                continue;
            }
            if (!needle.isEmpty()
                    && !hall.getId().toLowerCase().contains(needle)
                    && !hall.getName().toLowerCase().contains(needle)
                    && !hall.getTypeLabel().toLowerCase().contains(needle)) {
                continue;
            }
            matches.add(hall);
        }
        return matches;
    }

    // ---------- create, edit, delete ----------

    /** Adds a hall. Capacity and rate arrive as text straight from the form. */
    public Hall create(String name, HallType type, String capacityText, String rateText) {
        String hallName = Validator.text(name, "Hall name");
        if (type == null) {
            Validator.fail("Please choose a hall type.");
        }
        guardDuplicateName(hallName, null);

        Hall hall = new Hall(
                halls.nextId("H"),
                hallName,
                type,
                Validator.intInRange(capacityText, "Capacity", 1, MAX_CAPACITY),
                Validator.positiveDouble(rateText, "Rate per hour"),
                HallStatus.ACTIVE);
        halls.insert(hall);
        return hall;
    }

    /** Applies edits to an existing hall. */
    public void update(Hall hall, String name, HallType type, String capacityText,
                       String rateText) {
        String hallName = Validator.text(name, "Hall name");
        if (type == null) {
            Validator.fail("Please choose a hall type.");
        }
        guardDuplicateName(hallName, hall.getId());

        hall.setName(hallName);
        hall.setType(type);
        hall.setCapacity(Validator.intInRange(capacityText, "Capacity", 1, MAX_CAPACITY));
        hall.setRatePerHour(Validator.positiveDouble(rateText, "Rate per hour"));
        halls.update(hall);
    }

    /**
     * Withdraws a hall from sale.
     *
     * The record is marked INACTIVE rather than erased, so that past bookings can
     * still name the hall they were for. Refused while upcoming bookings exist.
     */
    public void delete(Hall hall) {
        int upcoming = countUpcomingBookings(hall.getId());
        if (upcoming > 0) {
            Validator.fail(hall.getName() + " has " + upcoming + " upcoming booking(s),"
                    + " so it cannot be deleted yet. Cancel those bookings first.");
        }
        hall.setStatus(HallStatus.INACTIVE);
        halls.update(hall);
    }

    /** Puts a withdrawn hall back on sale. */
    public void restore(Hall hall) {
        hall.setStatus(HallStatus.ACTIVE);
        halls.update(hall);
    }

    public int countUpcomingBookings(String hallId) {
        int count = 0;
        for (Booking booking : bookings.findActiveByHall(hallId)) {
            if (booking.isUpcoming()) {
                count++;
            }
        }
        return count;
    }

    private void guardDuplicateName(String name, String excludeHallId) {
        for (Hall existing : halls.findAll()) {
            if (existing.getName().equalsIgnoreCase(name)
                    && !existing.getId().equals(excludeHallId)) {
                Validator.fail("A hall named \"" + name + "\" already exists ("
                        + existing.getId() + ").");
            }
        }
    }
}
