package ui;

import models.User;
import services.AuthService;
import ui.components.BaseFrame;
import ui.components.FormPanel;
import ui.components.UiUtils;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * The entry screen.
 *
 * Once {@link AuthService} has identified the account, this class calls
 * {@code user.showDashboard()} and never asks which role signed in: each
 * {@link User} subclass knows which window to open.
 */
public class LoginFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final AuthService auth = new AuthService();
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        super("Login", 470, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(UiUtils.header("Hall Booking Management System",
                "Please sign in to continue."), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        usernameField = form.addTextField("Username:", "");
        passwordField = form.addPasswordField("Password:");
        form.addNote("Sample accounts - customer: john_customer / pass123");
        form.addNote("scheduler: mike_scheduler / sch123      manager: jane_manager / mgr123");
        form.addNote("administrator: sarah_admin / admin123");
        add(form, BorderLayout.CENTER);

        JButton loginButton = UiUtils.button("Login", this::handleLogin);
        JButton registerButton = UiUtils.button("Register", this::openRegistration);
        add(UiUtils.buttonRow(registerButton, loginButton), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton); // Enter submits the form
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        UiUtils.guarded(this, () -> {
            User user = auth.login(username, password);
            user.showDashboard(); // polymorphic: the role decides the screen
            dispose();
        });
    }

    private void openRegistration() {
        dispose();
        new RegistrationFrame().setVisible(true);
    }
}
