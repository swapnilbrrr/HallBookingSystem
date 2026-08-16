package models;

import models.enums.Role;
import models.enums.UserStatus;
import ui.admin.AdminDashboard;

/** Manages the staff and customer accounts. */
public class Administrator extends Staff {

    public Administrator(String id, String username, String password) {
        this(id, username, password, UserStatus.ACTIVE, "", "", "");
    }

    public Administrator(String id, String username, String password, UserStatus status,
                         String fullName, String email, String phone) {
        super(id, username, password, Role.ADMINISTRATOR, status, fullName, email, phone);
    }

    @Override
    public String getResponsibility() {
        return "Manage scheduler staff, user accounts and view all bookings.";
    }

    @Override
    public void showDashboard() {
        new AdminDashboard(this).setVisible(true);
    }
}
