package models;

import models.enums.Role;
import models.enums.UserStatus;
import ui.scheduler.SchedulerDashboard;

/** Handles booking and scheduling events in the halls. */
public class Scheduler extends Staff {

    public Scheduler(String id, String username, String password) {
        this(id, username, password, UserStatus.ACTIVE, "", "", "");
    }

    public Scheduler(String id, String username, String password, UserStatus status,
                     String fullName, String email, String phone) {
        super(id, username, password, Role.SCHEDULER, status, fullName, email, phone);
    }

    @Override
    public String getResponsibility() {
        return "Manage halls, availability schedules and maintenance schedules.";
    }

    @Override
    public void showDashboard() {
        new SchedulerDashboard(this).setVisible(true);
    }
}
