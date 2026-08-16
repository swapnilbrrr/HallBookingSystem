package models.enums;

/** Granularity of the Manager's sales dashboard. */
public enum SalesPeriod implements Labelled {

    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String label;

    SalesPeriod(String label) {
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
