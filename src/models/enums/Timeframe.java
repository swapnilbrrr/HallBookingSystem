package models.enums;

/** Whether a booking list shows everything, only future bookings, or only past ones. */
public enum Timeframe implements Labelled {

    ALL("All bookings"),
    UPCOMING("Upcoming only"),
    PAST("Past only");

    private final String label;

    Timeframe(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
