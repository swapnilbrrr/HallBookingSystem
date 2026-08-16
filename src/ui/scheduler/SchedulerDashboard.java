package ui.scheduler;

import models.Scheduler;
import ui.components.DashboardFrame;

/** Home screen for a scheduler. */
public class SchedulerDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    public SchedulerDashboard(Scheduler scheduler) {
        super(scheduler, "Scheduler Dashboard", scheduler.getResponsibility(), 660, 400);

        addAction("Hall Management", () -> new HallManagementFrame().setVisible(true));
        addAction("Hall Availability Schedule", () -> new AvailabilityFrame().setVisible(true));
        addAction("Hall Maintenance Schedule", () -> new MaintenanceFrame().setVisible(true));
        addAction("My Assigned Issues", () -> new AssignedIssuesFrame(scheduler).setVisible(true));
    }
}
