package repository;

import models.Maintenance;

import java.time.LocalDateTime;

/** Store for maintenance windows, during which a hall cannot be booked. */
public class MaintenanceRepository extends TimeSlotRepository<Maintenance> {

    public static final String FILE = "data/maintenance.txt";

    public MaintenanceRepository() {
        super(FILE,
                "# maintenance.txt",
                "# Windows reserved for maintenance work; bookings may not overlap these.",
                "# Format: id|hallId|start|end|remarks",
                "# start and end use ISO-8601, e.g. 2026-09-01T08:00");
    }

    @Override
    protected Maintenance createSlot(String id, String hallId, LocalDateTime start,
                                     LocalDateTime end, String remarks) {
        return new Maintenance(id, hallId, start, end, remarks);
    }
}
