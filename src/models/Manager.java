package models;

import models.enums.Role;
import models.enums.UserStatus;
import ui.manager.ManagerDashboard;

/** Oversees the performance of the sales and maintenance operations. */
public class Manager extends Staff {

    public Manager(String id, String username, String password) {
        this(id, username, password, UserStatus.ACTIVE, "", "", "");
    }

    public Manager(String id, String username, String password, UserStatus status,
                   String fullName, String email, String phone) {
        super(id, username, password, Role.MANAGER, status, fullName, email, phone);
    }

    @Override
    public String getResponsibility() {
        return "Review sales performance and resolve customer issues.";
    }

    @Override
    public void showDashboard() {
        new ManagerDashboard(this).setVisible(true);
    }
}
