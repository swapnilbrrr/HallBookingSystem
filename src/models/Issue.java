package models;

import models.enums.IssueStatus;
import repository.Persistable;
import utils.DateUtil;

import java.time.LocalDateTime;

/**
 * A complaint raised by a customer about a hall they booked, which the manager
 * responds to, assigns to a scheduler, and moves through the four statuses.
 *
 * Stored as:
 * id|customerId|hallId|bookingId|description|status|assignedSchedulerId|response|createdAt
 */
public class Issue implements Persistable {

    private final String id;
    private final String customerId;
    private final String hallId;
    private final String bookingId;
    private String description;
    private IssueStatus status;
    private String assignedSchedulerId;
    private String response;
    private final LocalDateTime createdAt;

    public Issue(String id, String customerId, String hallId, String bookingId,
                 String description, IssueStatus status, String assignedSchedulerId,
                 String response, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.hallId = hallId;
        this.bookingId = bookingId == null ? "" : bookingId;
        this.description = description;
        this.status = status == null ? IssueStatus.IN_PROGRESS : status;
        this.assignedSchedulerId = assignedSchedulerId == null ? "" : assignedSchedulerId;
        this.response = response == null ? "" : response;
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

    public String getBookingId() {
        return bookingId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public String getAssignedSchedulerId() {
        return assignedSchedulerId;
    }

    public void setAssignedSchedulerId(String assignedSchedulerId) {
        this.assignedSchedulerId = assignedSchedulerId == null ? "" : assignedSchedulerId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response == null ? "" : response;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---------- derived values ----------

    public boolean isAssigned() {
        return !assignedSchedulerId.isEmpty();
    }

    public boolean hasResponse() {
        return !response.isEmpty();
    }

    public boolean isOpen() {
        return !status.isFinal();
    }

    @Override
    public String toLine() {
        return Persistable.join(id, customerId, hallId, bookingId, description, status.name(),
                assignedSchedulerId, response, DateUtil.toStorage(createdAt));
    }

    @Override
    public String toString() {
        return id + " - " + description;
    }
}
