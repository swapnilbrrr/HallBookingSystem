package models.enums;

/**
 * Whether a hall is on offer.
 *
 * Deleting a hall marks it INACTIVE rather than erasing the record, so that
 * historical bookings can still resolve the hall they refer to.
 */
public enum HallStatus implements Labelled {

    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String label;

    HallStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static HallStatus fromStorage(String raw) {
        return Labelled.parse(HallStatus.class, raw, ACTIVE);
    }

    @Override
    public String toString() {
        return label;
    }
}
