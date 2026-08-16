package models;

import models.enums.Role;
import models.enums.UserStatus;

/**
 * Shared supertype for the three employee roles the brief lists as staff:
 * Scheduler, Administrator and Manager.
 *
 * Sitting between {@link User} and the concrete roles, it settles
 * {@link #isStaff()} once for all three instead of repeating it, and requires
 * each role to describe its own duties through {@link #getResponsibility()},
 * which the dashboards print in their header.
 */
public abstract class Staff extends User {

    protected Staff(String id, String username, String password, Role role,
                    UserStatus status, String fullName, String email, String phone) {
        super(id, username, password, role, status, fullName, email, phone);
    }

    @Override
    public final boolean isStaff() {
        return true;
    }

    /** One-line summary of what this staff member is responsible for. */
    public abstract String getResponsibility();
}
