package repository;

import models.Availability;

import java.time.LocalDateTime;

/** Store for the availability windows a scheduler opens on each hall. */
public class AvailabilityRepository extends TimeSlotRepository<Availability> {

    public static final String FILE = "data/availability.txt";

    public AvailabilityRepository() {
        super(FILE,
                "# availability.txt",
                "# Windows in which a scheduler has opened a hall for booking.",
                "# Format: id|hallId|start|end|remarks",
                "# start and end use ISO-8601, e.g. 2026-09-01T08:00");
    }

    @Override
    protected Availability createSlot(String id, String hallId, LocalDateTime start,
                                      LocalDateTime end, String remarks) {
        return new Availability(id, hallId, start, end, remarks);
    }
}
