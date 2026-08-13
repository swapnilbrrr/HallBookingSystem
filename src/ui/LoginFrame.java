package ui;

import services.AuthService;
import models.User;

import java.awt.LayoutManager;
import java.awt.GridLayout;

import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle("Hall Symphony Inc. - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout((LayoutManager) new GridLayout(3, 2, 10, 10)); // Keep this grid

        JLabel userLabel = new JLabel("  Username:");
        usernameField = new JTextField();
        
        JLabel passLabel = new JLabel("  Password:");
        passwordField = new JPasswordField();
        
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register"); // NEW BUTTON

        add(userLabel);
        add(usernameField);
        add(passLabel);
        add(passwordField);
        add(registerButton); // Add register button to the frame
        add(loginButton);

        loginButton.addActionListener(e -> handleLogin());
        
        // NEW ACTION LISTENER
        registerButton.addActionListener(e -> {
            this.dispose();
            new RegistrationFrame().setVisible(true);
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = AuthService.authenticate(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome " + user.getUsername());
            user.showDashboard();
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
