package ui.components;

import models.User;
import ui.LoginFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Shared shell for the four role dashboards: a welcome header, a grid of action
 * buttons, and a logout button that returns to the login screen.
 *
 * Subclasses only register their own actions, which is why each dashboard is a
 * few lines long rather than a repeated block of layout code.
 */
public abstract class DashboardFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final User user;
    private final JPanel menu = new JPanel(new GridLayout(0, 2, 12, 12));

    protected DashboardFrame(User user, String title, String subtitle, int width, int height) {
        super(title, width, height);
        this.user = user;

        // Closing the main window ends the program; child windows only dispose.
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(UiUtils.header("Welcome, " + user.getDisplayName(),
                user.getRoleLabel() + " - " + subtitle), BorderLayout.NORTH);

        menu.setBorder(BorderFactory.createEmptyBorder(6, 20, 16, 20));
        add(menu, BorderLayout.CENTER);

        JButton logout = UiUtils.button("Logout", this::logout);
        add(UiUtils.buttonRow(logout), BorderLayout.SOUTH);
    }

    /** Adds one labelled action to the dashboard's button grid. */
    protected void addAction(String label, Runnable action) {
        menu.add(UiUtils.button(label, action));
    }

    protected User getUser() {
        return user;
    }

    /** Signs out and returns to the login screen. */
    protected void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
