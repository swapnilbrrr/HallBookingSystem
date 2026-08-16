package ui.admin;

import models.User;
import ui.components.FormPanel;
import ui.components.UiUtils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Window;

/**
 * Add or edit a staff account.
 *
 * When editing, the password fields are hidden - passwords are changed through the
 * separate "Reset Password" action, so an edit cannot silently overwrite one.
 */
public class StaffFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /** Receives the entered values; supplied as a lambda by the calling screen. */
    public interface StaffSaver {
        void save(String username, String password, String fullName, String email, String phone);
    }

    private final StaffSaver saver;
    private final JTextField usernameField;
    private final JPasswordField passwordField; // null when editing
    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField phoneField;

    /**
     * @param existing the account being edited, or null when adding
     */
    public StaffFormDialog(Window owner, User existing, StaffSaver saver) {
        super(owner, existing == null ? "Add Staff" : "Edit " + existing.getId(),
                ModalityType.APPLICATION_MODAL);
        this.saver = saver;

        setLayout(new BorderLayout());
        add(UiUtils.header(existing == null ? "New staff account" : "Edit staff account",
                existing == null ? "The account is active as soon as it is created."
                        : "Use Reset Password to change the password."), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        if (existing != null) {
            form.addReadOnly("Staff ID:", existing.getId());
            form.addReadOnly("Role:", existing.getRoleLabel());
        }
        usernameField = form.addTextField("Username:",
                existing == null ? "" : existing.getUsername());
        passwordField = existing == null ? form.addPasswordField("Password:") : null;
        fullNameField = form.addTextField("Full name:",
                existing == null ? "" : existing.getFullName());
        emailField = form.addTextField("Email:", existing == null ? "" : existing.getEmail());
        phoneField = form.addTextField("Phone:", existing == null ? "" : existing.getPhone());
        form.addNote("Username: at least 3 characters. Email and phone are optional.");
        add(form, BorderLayout.CENTER);

        JButton save = UiUtils.button("Save", this::save);
        JButton cancel = UiUtils.button("Cancel", this::dispose);
        add(UiUtils.buttonRow(cancel, save), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(save);
        pack();
        setLocationRelativeTo(owner);
    }

    private void save() {
        String password = passwordField == null ? null : new String(passwordField.getPassword());
        boolean ok = UiUtils.guarded(this, () -> saver.save(
                usernameField.getText(),
                password,
                fullNameField.getText(),
                emailField.getText(),
                phoneField.getText()));
        if (ok) {
            dispose();
        }
    }
}
