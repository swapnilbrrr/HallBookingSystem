package models;

import repository.Persistable;
import utils.DateUtil;

import java.time.LocalDateTime;

/**
 * A dated window attached to a hall, with optional remarks.
 *
 * {@link Availability} and {@link Maintenance} differ only in meaning, so the
 * fields, the storage format and - most usefully - the overlap arithmetic live
 * here once. {@code BookingService} reuses {@link #overlaps} to reject clashes
 * and {@link #covers} to confirm a requested slot falls inside an open window.
 *
 * Stored as: id|hallId|start|end|remarks
 */
public abstract class TimeSlot implements Persistable {

    private final String id;
    private String hallId;
    private LocalDateTime start;
    private LocalDateTime end;
    private String remarks;

    protected TimeSlot(String id, String hallId, LocalDateTime start, LocalDateTime end,
                       String remarks) {
        this.id = id;
        this.hallId = hallId;
        this.start = start;
        this.end = end;
        this.remarks = remarks == null ? "" : remarks;
    }

    /** "Availability" or "Maintenance", for table headings and messages. */
    public abstract String getSlotType();

    // ---------- accessors ----------

    @Override
    public String getId() {
        return id;
    }

    public String getHallId() {
        return hallId;
    }

    public void setHallId(String hallId) {
        this.hallId = hallId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? "" : remarks;
    }

    // ---------- interval arithmetic ----------

    /**
     * True when this window and the given range share any time.
     *
     * The comparison is half-open, so a window ending exactly when another
     * begins is not a clash - 08:00-10:00 and 10:00-12:00 sit side by side.
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    /** True when both windows belong to the same hall and share any time. */
    public boolean overlaps(TimeSlot other) {
        return hallId.equals(other.hallId) && overlaps(other.start, other.end);
    }

    /** True when the given range sits entirely inside this window. */
    public boolean covers(LocalDateTime from, LocalDateTime to) {
        return !from.isBefore(start) && !to.isAfter(end);
    }

    public long getDurationHours() {
        return DateUtil.hoursBetween(start, end);
    }

    public boolean isPast() {
        return DateUtil.isPast(end);
    }

    public String getDisplayRange() {
        return DateUtil.displayRange(start, end);
    }

    @Override
    public String toLine() {
        return Persistable.join(id, hallId, DateUtil.toStorage(start), DateUtil.toStorage(end),
                remarks);
    }

    @Override
    public String toString() {
        return getSlotType() + " " + id + ": " + getDisplayRange();
    }
}
