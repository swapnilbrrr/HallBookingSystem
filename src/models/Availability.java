package models;

import java.time.LocalDateTime;

/**
 * A window in which a scheduler has opened a hall for booking.
 *
 * Customers only ever see halls that have an availability window covering the
 * slot they ask for, which is what the brief means by "view list of available
 * halls that has been set by the scheduler".
 */
public class Availability extends TimeSlot {

    public Availability(String id, String hallId, LocalDateTime start, LocalDateTime end,
                        String remarks) {
        super(id, hallId, start, end, remarks);
    }

    @Override
    public String getSlotType() {
        return "Availability";
    }
}
