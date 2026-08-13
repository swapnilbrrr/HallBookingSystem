package ui;
import models.Scheduler;
import javax.swing.*;
import java.awt.BorderLayout;

public class SchedulerDashboard extends JFrame {
    public SchedulerDashboard(Scheduler scheduler) {
        setTitle("Scheduler Dashboard");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        add(new JLabel("Welcome Scheduler: " + scheduler.getUsername(), SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel panel = new JPanel();
        panel.add(new JButton("Manage Halls"));
        panel.add(new JButton("Set Availability"));
        JButton logoutBtn = new JButton("Logout");
        panel.add(logoutBtn);
        add(panel, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
    }
}