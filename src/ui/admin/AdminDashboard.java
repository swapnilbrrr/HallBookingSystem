package ui.admin;

import models.Administrator;
import ui.components.DashboardFrame;

/** Home screen for an administrator. */
public class AdminDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    public AdminDashboard(Administrator administrator) {
        super(administrator, "Administrator Dashboard", administrator.getResponsibility(),
                660, 400);

        addAction("Scheduler Staff Management",
                () -> new SchedulerStaffFrame(administrator).setVisible(true));
        addAction("User Management",
                () -> new UserManagementFrame(administrator).setVisible(true));
        addAction("Booking Management", () -> new AllBookingsFrame().setVisible(true));
    }
}
