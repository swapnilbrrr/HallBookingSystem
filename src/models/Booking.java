package models;

import models.enums.BookingStatus;
import repository.Persistable;
import utils.DateUtil;

import java.time.LocalDateTime;

/**
 * One paid reservation of a hall.
 *
 * The booked duration is derived from the timestamps rather than stored, so the
 * hour count can never disagree with the start and end times.
 *
 * Stored as: id|customerId|hallId|start|end|totalCost|status|createdAt
 */
public class Booking implements Persistable {

    private final String id;
    private final String customerId;
    private final String hallId;
    private LocalDateTime start;
    private LocalDateTime end;
    private double totalCost;
    private BookingStatus status;
    private final LocalDateTime createdAt;

    public Booking(String id, String customerId, String hallId, LocalDateTime start,
                   LocalDateTime end, double totalCost, BookingStatus status,
                   LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.hallId = hallId;
        this.start = start;
        this.end = end;
        this.totalCost = totalCost;
        this.status = status == null ? BookingStatus.PAID : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    // ---------- accessors ----------

    @Override
    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getHallId() {
        return hallId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---------- derived values ----------

    public long getHours() {
        return DateUtil.hoursBetween(start, end);
    }

    public boolean isActive() {
        return status.isActive();
    }

    /** A live booking whose start time has not yet arrived. */
    public boolean isUpcoming() {
        return isActive() && !DateUtil.isPast(start);
    }

    public boolean isPast() {
        return DateUtil.isPast(start);
    }

    /** Only live bookings at least three days away may be cancelled. */
    public boolean isCancellable() {
        return isActive() && DateUtil.isCancellable(start);
    }

    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    public String getDisplayRange() {
        return DateUtil.displayRange(start, end);
    }

    @Override
    public String toLine() {
        return Persistable.join(id, customerId, hallId, DateUtil.toStorage(start),
                DateUtil.toStorage(end), String.format("%.2f", totalCost), status.name(),
                DateUtil.toStorage(createdAt));
    }

    @Override
    public String toString() {
        return id + " - " + getDisplayRange();
    }
}
