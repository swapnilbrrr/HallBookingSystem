package models;

public abstract class User {
    private String id;
    private String username;
    private String password;
    private String role; // Customer, Scheduler, Admin, Manager

    public User(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    
    // Abstract method to force subclasses to implement specific dashboard logic
    public abstract void showDashboard(); 
}