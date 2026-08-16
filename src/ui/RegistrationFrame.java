package ui;

import services.AuthService;
import ui.components.BaseFrame;
import ui.components.FormPanel;
import ui.components.UiUtils;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/** Self-registration for customers. Staff accounts are created by an administrator. */
public class RegistrationFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final AuthService auth = new AuthService();

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmField;
    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField phoneField;

    public RegistrationFrame() {
        super("Customer Registration", 500, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(UiUtils.header("Create a customer account",
                "Only the username and password are required."), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        usernameField = form.addTextField("Username:", "");
        passwordField = form.addPasswordField("Password:");
        confirmField = form.addPasswordField("Confirm password:");
        fullNameField = form.addTextField("Full name:", "");
        emailField = form.addTextField("Email:", "");
        phoneField = form.addTextField("Phone:", "");
        form.addNote("Username: at least 3 characters, letters, digits, dots, "
                + "underscores or hyphens.");
        form.addNote("Password: at least 5 characters.");
        add(form, BorderLayout.CENTER);

        JButton registerButton = UiUtils.button("Register", this::handleRegister);
        JButton backButton = UiUtils.button("Back to Login", this::backToLogin);
        add(UiUtils.buttonRow(backButton, registerButton), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(registerButton);
    }

    private void handleRegister() {
        boolean ok = UiUtils.guarded(this, () -> auth.registerCustomer(
                usernameField.getText(),
                new String(passwordField.getPassword()),
                new String(confirmField.getPassword()),
                fullNameField.getText(),
                emailField.getText(),
                phoneField.getText()));

        if (ok) {
            UiUtils.info(this, "Registration successful. You can now log in.");
            backToLogin();
        }
    }

    private void backToLogin() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
