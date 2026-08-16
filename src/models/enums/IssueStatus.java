package models.enums;

/**
 * The four issue states named in the brief:
 * In progress, Done, Closed, Cancelled.
 */
public enum IssueStatus implements Labelled {

    IN_PROGRESS("In progress", "Work on the task has started."),
    DONE("Done", "The task has been completed."),
    CLOSED("Closed", "The task is closed, and no further action is required."),
    CANCELLED("Cancelled", "The task has been cancelled and will not be completed.");

    private final String label;
    private final String description;

    IssueStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /** Closed and Cancelled issues need no further manager action. */
    public boolean isFinal() {
        return this == CLOSED || this == CANCELLED;
    }

    public static IssueStatus fromStorage(String raw) {
        return Labelled.parse(IssueStatus.class, raw, IN_PROGRESS);
    }

    @Override
    public String toString() {
        return label;
    }
}
