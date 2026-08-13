package models;
import ui.AdminDashboard;

public class Administrator extends User {
    public Administrator(String id, String username, String password) {
        super(id, username, password, "Administrator");
    }
    @Override
    public void showDashboard() { new AdminDashboard(this).setVisible(true); }
}