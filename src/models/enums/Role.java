package models.enums;

/** The four user types described in the coursework brief. */
public enum Role implements Labelled {

    CUSTOMER("Customer"),
    SCHEDULER("Scheduler"),
    ADMINISTRATOR("Administrator"),
    MANAGER("Manager");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /** Scheduler, Administrator and Manager are staff; Customer is not. */
    public boolean isStaff() {
        return this != CUSTOMER;
    }

    public static Role fromStorage(String raw) {
        return Labelled.parse(Role.class, raw, CUSTOMER);
    }

    @Override
    public String toString() {
        return label;
    }
}
