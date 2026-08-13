package models;
import ui.ManagerDashboard;

public class Manager extends User {
    public Manager(String id, String username, String password) {
        super(id, username, password, "Manager");
    }
    @Override
    public void showDashboard() { new ManagerDashboard(this).setVisible(true); }
}