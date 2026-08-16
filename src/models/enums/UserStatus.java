package models.enums;

/** Whether an account may sign in. Administrators toggle this. */
public enum UserStatus implements Labelled {

    ACTIVE("Active"),
    BLOCKED("Blocked");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static UserStatus fromStorage(String raw) {
        return Labelled.parse(UserStatus.class, raw, ACTIVE);
    }

    @Override
    public String toString() {
        return label;
    }
}
