package models;

public class Hall {
    public enum Type { AUDITORIUM, BANQUET, MEETING_ROOM }

    private String id;
    private String name;
    private Type type;
    private int capacity;

    public Hall(String id, String name, Type type, int capacity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        return "Hall{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", type=" + type + ", capacity=" + capacity + '}';
    }
}
