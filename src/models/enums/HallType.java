package models.enums;

/**
 * The three hall types offered by Hall Symphony Inc.
 *
 * Seating capacity and hourly rate come straight from Table 1 of the brief and
 * are held as enum state, so the pricing rules live in exactly one place:
 * auditorium RM 300.00, banquet hall RM 100.00, meeting room RM 50.00.
 */
public enum HallType implements Labelled {

    AUDITORIUM("Auditorium", 1000, 300.00),
    BANQUET_HALL("Banquet Hall", 300, 100.00),
    MEETING_ROOM("Meeting Room", 30, 50.00);

    private final String label;
    private final int defaultCapacity;
    private final double ratePerHour;

    HallType(String label, int defaultCapacity, double ratePerHour) {
        this.label = label;
        this.defaultCapacity = defaultCapacity;
        this.ratePerHour = ratePerHour;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public double getRatePerHour() {
        return ratePerHour;
    }

    public static HallType fromStorage(String raw) {
        return Labelled.parse(HallType.class, raw, MEETING_ROOM);
    }

    @Override
    public String toString() {
        return label;
    }
}
