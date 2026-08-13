package ui;

import models.Customer;
import ui.LoginFrame;
import javax.swing.*;
import java.awt.*;

public class CustomerDashboard extends JFrame {
    private Customer loggedInCustomer;

    public CustomerDashboard(Customer customer) {
        this.loggedInCustomer = customer;

        setTitle("Customer Dashboard - " + customer.getUsername());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Welcome Banner
        JLabel welcomeLabel = new JLabel("Welcome, " + customer.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Center Menu (Buttons for features)
        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton viewHallsBtn = new JButton("View Available Halls");
        JButton viewBookingsBtn = new JButton("My Bookings");
        JButton raiseIssueBtn = new JButton("Raise an Issue");
        JButton logoutBtn = new JButton("Logout");

        menuPanel.add(viewHallsBtn);
        menuPanel.add(viewBookingsBtn);
        menuPanel.add(raiseIssueBtn);
        menuPanel.add(new JLabel("")); // Spacer
        menuPanel.add(logoutBtn);

        add(menuPanel, BorderLayout.CENTER);

        // Button Actions
        logoutBtn.addActionListener(e -> {
            this.dispose(); // Close dashboard
            new LoginFrame().setVisible(true); // Back to login
        });
        
        // TODO: Add action listeners for the other buttons to handle booking logic
    }
}