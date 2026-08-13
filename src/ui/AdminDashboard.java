package ui;
import models.Administrator;
import javax.swing.*;
import java.awt.BorderLayout;

public class AdminDashboard extends JFrame {
    public AdminDashboard(Administrator admin) {
        setTitle("Admin Dashboard");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        add(new JLabel("Welcome Admin: " + admin.getUsername(), SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel panel = new JPanel();
        panel.add(new JButton("Manage Staff"));
        panel.add(new JButton("Manage Users"));
        JButton logoutBtn = new JButton("Logout");
        panel.add(logoutBtn);
        add(panel, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
    }
}