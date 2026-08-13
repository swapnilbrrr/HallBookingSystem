package models;

public class Hall {
    private String id;
    private String name;
    private String type;
    private int capacity;
    private double ratePerHour;

    public Hall(String id, String name, String type, int capacity, double ratePerHour) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.ratePerHour = ratePerHour;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public double getRatePerHour() { return ratePerHour; }
}