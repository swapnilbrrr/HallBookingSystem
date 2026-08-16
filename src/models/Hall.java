package models;

import models.enums.HallStatus;
import models.enums.HallType;
import repository.Persistable;

/**
 * A bookable hall.
 *
 * Stored as: id|name|type|capacity|ratePerHour|status
 */
public class Hall implements Persistable {

    private final String id;
    private String name;
    private HallType type;
    private int capacity;
    private double ratePerHour;
    private HallStatus status;

    /** Creates a hall using the standard capacity and rate for its type. */
    public Hall(String id, String name, HallType type) {
        this(id, name, type, type.getDefaultCapacity(), type.getRatePerHour(), HallStatus.ACTIVE);
    }

    public Hall(String id, String name, HallType type, int capacity, double ratePerHour,
                HallStatus status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.ratePerHour = ratePerHour;
        this.status = status == null ? HallStatus.ACTIVE : status;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HallType getType() {
        return type;
    }

    public void setType(HallType type) {
        this.type = type;
    }

    public String getTypeLabel() {
        return type.getLabel();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getRatePerHour() {
        return ratePerHour;
    }

    public void setRatePerHour(double ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    public HallStatus getStatus() {
        return status;
    }

    public void setStatus(HallStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return status == HallStatus.ACTIVE;
    }

    /** Total charge for the given number of hours at this hall's rate. */
    public double costFor(long hours) {
        return ratePerHour * hours;
    }

    @Override
    public String toLine() {
        return Persistable.join(id, name, type.name(), capacity,
                String.format("%.2f", ratePerHour), status.name());
    }

    /** Label used by combo boxes on the booking and scheduling screens. */
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
