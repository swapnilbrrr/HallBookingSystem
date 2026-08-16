package models;

import java.time.LocalDateTime;

/**
 * A window reserved for maintenance work, for example
 * "Air conditioning check and lighting repairs".
 *
 * Bookings that overlap a maintenance window are refused.
 */
public class Maintenance extends TimeSlot {

    public Maintenance(String id, String hallId, LocalDateTime start, LocalDateTime end,
                       String remarks) {
        super(id, hallId, start, end, remarks);
    }

    @Override
    public String getSlotType() {
        return "Maintenance";
    }
}
