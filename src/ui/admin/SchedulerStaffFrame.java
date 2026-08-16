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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Scheduler Staff Management: add, view and filter, edit, and delete scheduler
 * staff accounts.
 */
public class SchedulerStaffFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Administrator administrator;
    private final UserService users = new UserService();
    private final TablePanel<User> table;

    private final JTextField searchField = new JTextField(14);
    private final JLabel summaryLabel = new JLabel();

    public SchedulerStaffFrame(Administrator administrator) {
        super("Scheduler Staff Management", 900, 500);
        this.administrator = administrator;

        add(UiUtils.header("Scheduler staff",
                "Scheduler accounts can manage halls, availability and maintenance."),
                BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"Staff ID", "Username", "Full name", "Email", "Phone", "Status"},
                user -> new Object[]{
                        user.getId(),
                        user.getUsername(),
                        UiUtils.orDash(user.getFullName()),
                        UiUtils.orDash(user.getEmail()),
                        UiUtils.orDash(user.getPhone()),
                        user.getStatus().getLabel()});
        table.setColumnWidth(2, 150);
        table.setColumnWidth(3, 200);

        JPanel filters = UiUtils.filterRow(
                new JLabel("Search:"), searchField,
                UiUtils.button("Apply", this::refresh),
                UiUtils.button("Reset", this::reset));

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(filters, BorderLayout.NORTH);
        centre.add(table, BorderLayout.CENTER);
        centre.add(summaryLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        JButton add = UiUtils.button("Add Scheduler", this::addStaff);
        JButton edit = UiUtils.button("Edit Details", this::editStaff);
        JButton reset = UiUtils.button("Reset Password", this::resetPassword);
        JButton delete = UiUtils.button("Delete", this::deleteStaff);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, delete, reset, edit, add), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        List<User> rows = users.filter(searchField.getText(), Role.SCHEDULER, null);
        table.setRows(rows);
        int active = 0;
        for (User user : rows) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                active++;
            }
        }
        summaryLabel.setText(rows.size() + " scheduler(s) shown, " + active + " active.");
    }

    private void reset() {
        searchField.setText("");
        refresh();
    }

    private void addStaff() {
        new StaffFormDialog(this, null, (username, password, fullName, email, phone) ->
                users.createStaff(Role.SCHEDULER, username, password, fullName, email, phone))
                .setVisible(true);
        refresh();
    }

    private void editStaff() {
        User selected = requireSelection("edit");
        if (selected == null) {
            return;
        }
        new StaffFormDialog(this, selected, (username, password, fullName, email, phone) ->
                users.updateAccount(selected, username, fullName, email, phone))
                .setVisible(true);
        refresh();
    }

    private void resetPassword() {
        User selected = requireSelection("reset the password for");
        if (selected == null) {
            return;
        }
        String newPassword = JOptionPane.showInputDialog(this,
                "New password for " + selected.getUsername() + ":", "Reset Password",
                JOptionPane.QUESTION_MESSAGE);
        if (newPassword == null) {
            return; // cancelled
        }
        if (UiUtils.guarded(this, () -> users.resetPassword(selected, newPassword))) {
            UiUtils.info(this, "Password updated for " + selected.getUsername() + ".");
        }
    }

    private void deleteStaff() {
        User selected = requireSelection("delete");
        if (selected == null) {
            return;
        }
        if (!UiUtils.confirm(this, "Permanently delete scheduler " + selected.getUsername()
                + " (" + selected.getId() + ")?")) {
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
            UiUtils.error(this, "Please select a scheduler to " + action + ".");
        }
        return selected;
    }
}
