package models.enums;

/** Lifecycle of a booking. Payment is taken up front, so a live booking is PAID. */
public enum BookingStatus implements Labelled {

    PAID("Paid"),
    CANCELLED("Cancelled");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /** Cancelled bookings free the slot and are excluded from sales totals. */
    public boolean isActive() {
        return this == PAID;
    }

    public static BookingStatus fromStorage(String raw) {
        return Labelled.parse(BookingStatus.class, raw, PAID);
    }

    @Override
    public String toString() {
        return label;
    }
}
