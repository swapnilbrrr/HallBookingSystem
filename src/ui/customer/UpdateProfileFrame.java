package ui.customer;

import models.Customer;
import services.AuthService;
import ui.LoginFrame;
import ui.components.BaseFrame;
import ui.components.FormPanel;
import ui.components.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * "Update profile information": contact details and password, in one window.
 *
 * Changing the password signs the customer out, since the stored credentials no
 * longer match the ones they logged in with.
 */
public class UpdateProfileFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final AuthService auth = new AuthService();

    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField phoneField;

    private final JPasswordField currentPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;

    public UpdateProfileFrame(Customer customer) {
        super("Update Profile", 540, 520);
        this.customer = customer;

        add(UiUtils.header("My profile",
                "Signed in as " + customer.getUsername() + " (" + customer.getId() + ")"),
                BorderLayout.NORTH);

        FormPanel details = new FormPanel();
        details.addReadOnly("Username:", customer.getUsername());
        details.addReadOnly("Role:", customer.getRoleLabel());
        fullNameField = details.addTextField("Full name:", customer.getFullName());
        emailField = details.addTextField("Email:", customer.getEmail());
        phoneField = details.addTextField("Phone:", customer.getPhone());

        FormPanel password = new FormPanel();
        currentPasswordField = password.addPasswordField("Current password:");
        newPasswordField = password.addPasswordField("New password:");
        confirmPasswordField = password.addPasswordField("Confirm new password:");
        password.addNote("At least 5 characters. You will be asked to log in again.");

        JPanel detailsSection = section("Contact details", details,
                UiUtils.button("Save Details", this::saveDetails));
        JPanel passwordSection = section("Change password", password,
                UiUtils.button("Change Password", this::changePassword));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(detailsSection, BorderLayout.NORTH);
        centre.add(passwordSection, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        add(UiUtils.buttonRow(UiUtils.button("Close", this::dispose)), BorderLayout.SOUTH);
    }

    /** Titled block: heading, fields, and its own action button. */
    private static JPanel section(String title, JPanel fields, JButton action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(fields, BorderLayout.CENTER);
        panel.add(UiUtils.buttonRow(action), BorderLayout.SOUTH);
        return panel;
    }

    private void saveDetails() {
        boolean ok = UiUtils.guarded(this, () -> auth.updateProfile(customer,
                fullNameField.getText(), emailField.getText(), phoneField.getText()));
        if (ok) {
            UiUtils.info(this, "Your profile has been updated.");
        }
    }

    private void changePassword() {
        boolean ok = UiUtils.guarded(this, () -> auth.changePassword(customer,
                new String(currentPasswordField.getPassword()),
                new String(newPasswordField.getPassword()),
                new String(confirmPasswordField.getPassword())));
        if (ok) {
            UiUtils.info(this, "Your password has been changed. Please log in again.");
            // Close every open window and return to a clean login screen.
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                window.dispose();
            }
            new LoginFrame().setVisible(true);
        }
    }
}
