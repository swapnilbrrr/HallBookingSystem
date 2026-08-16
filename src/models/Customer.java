package models;

import models.enums.Role;
import models.enums.UserStatus;
import ui.customer.CustomerDashboard;

/** The one who reserves halls for their events. */
public class Customer extends User {

    /** Convenience constructor used at registration, before a profile is filled in. */
    public Customer(String id, String username, String password) {
        this(id, username, password, UserStatus.ACTIVE, "", "", "");
    }

    public Customer(String id, String username, String password, UserStatus status,
                    String fullName, String email, String phone) {
        super(id, username, password, Role.CUSTOMER, status, fullName, email, phone);
    }

    @Override
    public boolean isStaff() {
        return false;
    }

    @Override
    public void showDashboard() {
        new CustomerDashboard(this).setVisible(true);
    }
}
