package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {
    public enum Status { PENDING, CONFIRMED, CANCELLED }

    private String id;
    private String hallId;
    private String userId;
    private LocalDateTime start;
    private LocalDateTime end;
    private Status status;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Booking(String id, String hallId, String userId, LocalDateTime start, LocalDateTime end, Status status) {
        this.id = id;
        this.hallId = hallId;
        this.userId = userId;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    public String getId() { return id; }
    public String getHallId() { return hallId; }
    public String getUserId() { return userId; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return id + "|" + hallId + "|" + userId + "|" + start.format(FMT) + "|" + end.format(FMT) + "|" + status;
    }

    public static Booking fromLine(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;
        LocalDateTime s = LocalDateTime.parse(parts[3]);
        LocalDateTime e = LocalDateTime.parse(parts[4]);
        Status st = Status.valueOf(parts[5]);
        return new Booking(parts[0], parts[1], parts[2], s, e, st);
    }
}
