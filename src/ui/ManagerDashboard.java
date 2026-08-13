package ui;
import models.Manager;
import javax.swing.*;
import java.awt.BorderLayout;

public class ManagerDashboard extends JFrame {
    public ManagerDashboard(Manager manager) {
        setTitle("Manager Dashboard");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        add(new JLabel("Welcome Manager: " + manager.getUsername(), SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel panel = new JPanel();
        panel.add(new JButton("Sales Dashboard"));
        panel.add(new JButton("Maintenance Operations"));
        JButton logoutBtn = new JButton("Logout");
        panel.add(logoutBtn);
        add(panel, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
    }
}