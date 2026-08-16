package models;

import models.enums.Role;
import models.enums.UserStatus;
import repository.Persistable;

/**
 * Base type for every account in the system.
 *
 * The class is abstract because a bare "user" cannot exist: each account is a
 * Customer or one of the three staff roles. Two abstract methods drive the
 * polymorphism the rest of the program relies on - {@link #showDashboard()},
 * which lets {@code LoginFrame} open the right screen without testing the role,
 * and {@link #isStaff()}.
 *
 * Stored as: id|username|password|role|status|fullName|email|phone
 */
public abstract class User implements Persistable {

    private final String id;
    private String username;
    private String password;
    private final Role role;
    private UserStatus status;
    private String fullName;
    private String email;
    private String phone;

    protected User(String id, String username, String password, Role role,
                   UserStatus status, String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status == null ? UserStatus.ACTIVE : status;
        this.fullName = fullName == null ? "" : fullName;
        this.email = email == null ? "" : email;
        this.phone = phone == null ? "" : phone;
    }

    /** Opens the dashboard belonging to this role. */
    public abstract void showDashboard();

    /** True for Scheduler, Administrator and Manager; false for Customer. */
    public abstract boolean isStaff();

    // ---------- accessors ----------

    @Override
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    /** Display text for tables and dashboard headers. */
    public String getRoleLabel() {
        return role.getLabel();
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? "" : fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? "" : phone;
    }

    // ---------- behaviour ----------

    /** Blocked accounts are refused at the login screen. */
    public boolean canLogin() {
        return status == UserStatus.ACTIVE;
    }

    /** Real name when known, otherwise the username. */
    public String getDisplayName() {
        return fullName.isEmpty() ? username : fullName;
    }

    @Override
    public String toLine() {
        return Persistable.join(id, username, password, role.name(), status.name(),
                fullName, email, phone);
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + id + ")";
    }
}
