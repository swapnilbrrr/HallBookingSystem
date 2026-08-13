package models;
import ui.SchedulerDashboard;

public class Scheduler extends User {
    public Scheduler(String id, String username, String password) {
        super(id, username, password, "Scheduler");
    }
    @Override
    public void showDashboard() { new SchedulerDashboard(this).setVisible(true); }
}