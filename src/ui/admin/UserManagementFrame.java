package ui.admin;

import models.Administrator;
import models.User;
import models.enums.Role;
import models.enums.UserStatus;
import services.UserService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.List;

/**
 * User Management: view and filter every account, and block, unblock or delete one.
 *
 * Blocking is reversible and keeps the history intact; deletion is refused while a
 * customer still has upcoming bookings.
 */
public class UserManagementFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Administrator administrator;
    private final UserService users = new UserService();
    private final TablePanel<User> table;

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<Object> roleFilter =
            new JComboBox<>(UiUtils.anyOption("All roles", Role.values()));
    private final JComboBox<Object> statusFilter =
            new JComboBox<>(UiUtils.anyOption("All statuses", UserStatus.values()));
    private final JLabel summaryLabel = new JLabel();

    public UserManagementFrame(Administrator administrator) {
        super("User Management", 940, 520);
        this.administrator = administrator;

        add(UiUtils.header("All user accounts",
                "Block an account to deny sign-in without losing its history."),
                BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"User ID", "Username", "Full name", "Role", "Email", "Phone",
                        "Status"},
                user -> new Object[]{
                        user.getId(),
                        user.getUsername(),
                        UiUtils.orDash(user.getFullName()),
                        user.getRoleLabel(),
                        UiUtils.orDash(user.getEmail()),
                        UiUtils.orDash(user.getPhone()),
                        user.getStatus().getLabel()});
        table.setColumnWidth(2, 150);
        table.setColumnWidth(4, 190);

        roleFilter.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> refresh());

        JPanel filters = UiUtils.filterRow(
                new JLabel("Search:"), searchField,
                new JLabel("Role:"), roleFilter,
                new JLabel("Status:"), statusFilter,
                UiUtils.button("Apply", this::refresh),
                UiUtils.button("Reset", this::reset));

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(filters, BorderLayout.NORTH);
        centre.add(table, BorderLayout.CENTER);
        centre.add(summaryLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        JButton block = UiUtils.button("Block", () -> setStatus(UserStatus.BLOCKED));
        JButton unblock = UiUtils.button("Unblock", () -> setStatus(UserStatus.ACTIVE));
        JButton delete = UiUtils.button("Delete", this::deleteUser);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, delete, unblock, block), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        List<User> rows = users.filter(
                searchField.getText(),
                UiUtils.selected(roleFilter, Role.class),
                UiUtils.selected(statusFilter, UserStatus.class));
        table.setRows(rows);

        int blocked = 0;
        for (User user : rows) {
            if (user.getStatus() == UserStatus.BLOCKED) {
                blocked++;
            }
        }
        summaryLabel.setText(rows.size() + " account(s) shown, " + blocked + " blocked."
                + " Total accounts: " + users.findAll().size() + ".");
    }

    private void reset() {
        searchField.setText("");
        roleFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        refresh();
    }

    private void setStatus(UserStatus status) {
        User selected = requireSelection(status == UserStatus.BLOCKED ? "block" : "unblock");
        if (selected == null) {
            return;
        }
        if (selected.getStatus() == status) {
            UiUtils.error(this, selected.getUsername() + " is already "
                    + status.getLabel().toLowerCase() + ".");
            return;
        }
        if (UiUtils.guarded(this, () -> users.setStatus(administrator, selected, status))) {
            UiUtils.info(this, selected.getUsername() + " is now "
                    + status.getLabel().toLowerCase() + ".");
            refresh();
        }
    }

    private void deleteUser() {
        User selected = requireSelection("delete");
        if (selected == null) {
            return;
        }
        if (!UiUtils.confirm(this, "Permanently delete " + selected.getUsername()
                + " (" + selected.getId() + ")?\nBlocking is reversible; deletion is not.")) {
            return;
        }
        if (UiUtils.guarded(this, () -> users.delete(administrator, selected))) {
            UiUtils.info(this, selected.getUsername() + " has been deleted.");
            refresh();
        }
    }

    private User requireSelection(String action) {
        User selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select an account to " + action + ".");
        }
        return selected;
    }
}
