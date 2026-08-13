package ui;

import services.AuthService;
import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public RegistrationFrame() {
        setTitle("Customer Registration");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout((LayoutManager) new GridLayout(3, 2, 10, 10));

        add(new JLabel("  New Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("  New Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");

        add(backBtn);
        add(registerBtn);

        backBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
                return;
            }

            AuthService auth = new AuthService();
            if (auth.registerCustomer(user, pass)) {
                JOptionPane.showMessageDialog(this, "Registration Successful! Please login.");
                this.dispose();
                new LoginFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists.");
            }
        });
    }
}