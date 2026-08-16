package ui.manager;

import models.Manager;
import ui.components.DashboardFrame;

/** Home screen for a manager. */
public class ManagerDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    public ManagerDashboard(Manager manager) {
        super(manager, "Manager Dashboard", manager.getResponsibility(), 660, 400);

        addAction("Sales Dashboard", () -> new SalesDashboardFrame().setVisible(true));
        addAction("Maintenance Operations", () -> new IssueManagementFrame().setVisible(true));
    }
}
