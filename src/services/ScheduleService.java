package services;

import models.Availability;
import models.Booking;
import models.Maintenance;
import models.TimeSlot;
import repository.AvailabilityRepository;
import repository.BookingRepository;
import repository.HallRepository;
import repository.MaintenanceRepository;
import utils.DateUtil;
import utils.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Scheduler's two schedule screens: hall availability and hall maintenance.
 *
 * Both kinds of window are validated the same way, so the shared checks are
 * written once against {@link TimeSlot} and reused for each.
 */
public class ScheduleService {

    private final AvailabilityRepository availabilityStore = new AvailabilityRepository();
    private final MaintenanceRepository maintenanceStore = new MaintenanceRepository();
    private final BookingRepository bookings = new BookingRepository();
    private final HallRepository halls = new HallRepository();

    // ---------- availability ----------

    public List<Availability> findAllAvailability() {
        return availabilityStore.findAll();
    }

    public List<Availability> findAvailabilityByHall(String hallId) {
        return availabilityStore.findByHall(hallId);
    }

    public Availability addAvailability(String hallId, String startDate, String startTime,
                                        String endDate, String endTime, String remarks) {
        LocalDateTime start = DateUtil.parseInputDateTime(startDate, startTime, "Start");
        LocalDateTime end = DateUtil.parseInputDateTime(endDate, endTime, "End");
        String notes = Validator.optionalText(remarks, "Remarks");

        guardHall(hallId);
        guardRange(start, end);
        guardOverlap(availabilityStore.findByHall(hallId), null, start, end, "availability window");

        Availability slot = new Availability(availabilityStore.nextId("A"), hallId, start, end,
                notes);
        availabilityStore.insert(slot);
        return slot;
    }

    public void updateAvailability(Availability slot, String hallId, String startDate,
                                   String startTime, String endDate, String endTime,
                                   String remarks) {
        LocalDateTime start = DateUtil.parseInputDateTime(startDate, startTime, "Start");
        LocalDateTime end = DateUtil.parseInputDateTime(endDate, endTime, "End");
        String notes = Validator.optionalText(remarks, "Remarks");

        guardHall(hallId);
        guardRange(start, end);
        guardOverlap(availabilityStore.findByHall(hallId), slot.getId(), start, end,
                "availability window");

        applyTo(slot, hallId, start, end, notes);
        availabilityStore.update(slot);
    }

    public void deleteAvailability(String id) {
        availabilityStore.delete(id);
    }

    // ---------- maintenance ----------

    public List<Maintenance> findAllMaintenance() {
        return maintenanceStore.findAll();
    }

    public List<Maintenance> findMaintenanceByHall(String hallId) {
        return maintenanceStore.findByHall(hallId);
    }

    public Maintenance addMaintenance(String hallId, String startDate, String startTime,
                                      String endDate, String endTime, String remarks) {
        LocalDateTime start = DateUtil.parseInputDateTime(startDate, startTime, "Start");
        LocalDateTime end = DateUtil.parseInputDateTime(endDate, endTime, "End");
        String notes = Validator.optionalText(remarks, "Remarks");

        guardHall(hallId);
        guardRange(start, end);
        guardOverlap(maintenanceStore.findByHall(hallId), null, start, end, "maintenance window");
        guardBookedDuringMaintenance(hallId, start, end);

        Maintenance slot = new Maintenance(maintenanceStore.nextId("M"), hallId, start, end, notes);
        maintenanceStore.insert(slot);
        return slot;
    }

    public void updateMaintenance(Maintenance slot, String hallId, String startDate,
                                  String startTime, String endDate, String endTime,
                                  String remarks) {
        LocalDateTime start = DateUtil.parseInputDateTime(startDate, startTime, "Start");
        LocalDateTime end = DateUtil.parseInputDateTime(endDate, endTime, "End");
        String notes = Validator.optionalText(remarks, "Remarks");

        guardHall(hallId);
        guardRange(start, end);
        guardOverlap(maintenanceStore.findByHall(hallId), slot.getId(), start, end,
                "maintenance window");
        guardBookedDuringMaintenance(hallId, start, end);

        applyTo(slot, hallId, start, end, notes);
        maintenanceStore.update(slot);
    }

    public void deleteMaintenance(String id) {
        maintenanceStore.delete(id);
    }

    // ---------- shared helpers ----------

    /** Live bookings that fall inside a window, so a screen can confirm before removing it. */
    public int countBookingsInside(TimeSlot slot) {
        int count = 0;
        for (Booking booking : bookings.findActiveByHall(slot.getHallId())) {
            if (slot.overlaps(booking.getStart(), booking.getEnd())) {
                count++;
            }
        }
        return count;
    }

    private static void applyTo(TimeSlot slot, String hallId, LocalDateTime start,
                                LocalDateTime end, String remarks) {
        slot.setHallId(hallId);
        slot.setStart(start);
        slot.setEnd(end);
        slot.setRemarks(remarks);
    }

    private void guardHall(String hallId) {
        if (hallId == null || hallId.trim().isEmpty()) {
            Validator.fail("Please choose a hall.");
        }
        if (!halls.existsById(hallId)) {
            Validator.fail("Hall " + hallId + " does not exist.");
        }
    }

    private static void guardRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            Validator.fail("The end date and time must come after the start date and time.");
        }
    }

    /** Rejects a window that clashes with another of the same kind on the same hall. */
    private static void guardOverlap(List<? extends TimeSlot> existing, String excludeId,
                                     LocalDateTime start, LocalDateTime end, String kind) {
        for (TimeSlot slot : existing) {
            if (excludeId != null && slot.getId().equals(excludeId)) {
                continue;
            }
            if (slot.overlaps(start, end)) {
                Validator.fail("This clashes with an existing " + kind + " ("
                        + slot.getId() + ", " + slot.getDisplayRange() + ").");
            }
        }
    }

    /** A hall cannot be taken down for maintenance while it is already booked. */
    private void guardBookedDuringMaintenance(String hallId, LocalDateTime start,
                                              LocalDateTime end) {
        List<String> clashes = new ArrayList<>();
        for (Booking booking : bookings.findActiveByHall(hallId)) {
            if (booking.overlaps(start, end)) {
                clashes.add(booking.getId() + " (" + booking.getDisplayRange() + ")");
            }
        }
        if (!clashes.isEmpty()) {
            Validator.fail("This hall is already booked during that period: "
                    + String.join(", ", clashes)
                    + ". Choose another time or cancel the booking first.");
        }
    }

    public Optional<Availability> findAvailabilityById(String id) {
        return availabilityStore.findById(id);
    }

    public Optional<Maintenance> findMaintenanceById(String id) {
        return maintenanceStore.findById(id);
    }
}
