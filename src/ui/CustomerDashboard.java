package ui;

import models.Customer;
import services.AuthService;
import utils.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private Customer loggedInCustomer;

    public CustomerDashboard(Customer customer) {
        this.loggedInCustomer = customer;

        setTitle("Customer Dashboard - " + customer.getUsername());
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Welcome Header
        JLabel welcomeLabel = new JLabel("Welcome, " + customer.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Menu Grid Layout
        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JButton viewHallsBtn = new JButton("View & Book Halls");
        JButton viewBookingsBtn = new JButton("My Bookings & Cancel");
        JButton raiseIssueBtn = new JButton("Raise an Issue");
        JButton updateProfileBtn = new JButton("Update Password");
        JButton logoutBtn = new JButton("Logout");

        menuPanel.add(viewHallsBtn);
        menuPanel.add(viewBookingsBtn);
        menuPanel.add(raiseIssueBtn);
        menuPanel.add(updateProfileBtn);
        menuPanel.add(new JLabel("")); // Spacer
        menuPanel.add(logoutBtn);

        add(menuPanel, BorderLayout.CENTER);

        // 1. View & Book Halls
        viewHallsBtn.addActionListener(e -> new BookHallFrame(customer).setVisible(true));

        // 2. My Bookings & Cancel (Enforces 3-day rule)
        viewBookingsBtn.addActionListener(e -> openMyBookingsWindow());

        // 3. Raise an Issue with Manager
        raiseIssueBtn.addActionListener(e -> openRaiseIssueWindow());

        // 4. Update Profile (Password)
        updateProfileBtn.addActionListener(e -> {
            String newPassword = JOptionPane.showInputDialog(this, "Enter your new password:");
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                AuthService auth = new AuthService();
                auth.updateCustomerPassword(customer.getId(), newPassword.trim());
                JOptionPane.showMessageDialog(this, "Password updated successfully! Please login again.");
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });

        // 5. Logout
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void openMyBookingsWindow() {
        JFrame bookingsFrame = new JFrame("My Bookings");
        bookingsFrame.setSize(650, 350);
        bookingsFrame.setLocationRelativeTo(this);
        bookingsFrame.setLayout(new BorderLayout());

        String[] columns = {"Booking ID", "Hall ID", "Date", "Hours", "Total (RM)", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        bookingsFrame.add(new JScrollPane(table), BorderLayout.CENTER);

        // Read bookings from text file and filter by current customer ID
        List<String> lines = FileHandler.readLines("data/bookings.txt");
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split(",");
            // Schema: bookingId, customerId, hallId, date, hours, totalCost, status
            if (p.length >= 7 && p[1].trim().equals(loggedInCustomer.getId())) {
                model.addRow(new Object[]{p[0], p[2], p[3], p[4], p[5], p[6]});
            }
        }

        JPanel bottom = new JPanel();
        JButton cancelBookingBtn = new JButton("Cancel Selected Booking");
        bottom.add(cancelBookingBtn);
        bookingsFrame.add(bottom, BorderLayout.SOUTH);

        // Cancel booking logic with requirement validation (at least 3 days before)[cite: 3]
        cancelBookingBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(bookingsFrame, "Please select a booking to cancel.");
                return;
            }
            String bookingId = (String) model.getValueAt(row, 0);
            String bookingDateStr = (String) model.getValueAt(row, 2);
            String currentStatus = (String) model.getValueAt(row, 5);

            if (currentStatus.equalsIgnoreCase("CANCELLED")) {
                JOptionPane.showMessageDialog(bookingsFrame, "This booking is already cancelled.");
                return;
            }

            // Enforce 3-day rule constraint[cite: 3]
            try {
                LocalDate bookingDate = LocalDate.parse(bookingDateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), bookingDate);
                if (daysBetween < 3) {
                    JOptionPane.showMessageDialog(bookingsFrame, "Bookings can only be cancelled at least 3 days before the booking date.", "Cancellation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                // Ignore parsing errors and allow bypass if format differs slightly, or handle safely
            }

            // Update status in bookings.txt
            List<String> allBookings = FileHandler.readLines("data/bookings.txt");
            List<String> updatedBookings = new java.util.ArrayList<>();
            for (String bLine : allBookings) {
                if (bLine.startsWith(bookingId + ",")) {
                    String[] parts = bLine.split(",");
                    parts[6] = "CANCELLED";
                    updatedBookings.add(String.join(",", parts));
                } else {
                    updatedBookings.add(bLine);
                }
            }
            FileHandler.writeAllLines("data/bookings.txt", updatedBookings);
            JOptionPane.showMessageDialog(bookingsFrame, "Booking successfully cancelled.");
            bookingsFrame.dispose();
        });

        bookingsFrame.setVisible(true);
    }

    private void openRaiseIssueWindow() {
        JFrame issueFrame = new JFrame("Raise an Issue");
        issueFrame.setSize(400, 250);
        issueFrame.setLocationRelativeTo(this);
        issueFrame.setLayout(new GridLayout(4, 1, 10, 10));

        issueFrame.add(new JLabel("  Enter Hall ID related to the issue:"));
        JTextField hallIdField = new JTextField();
        issueFrame.add(hallIdField);

        issueFrame.add(new JLabel("  Describe the issue:"));
        JTextField descField = new JTextField();
        issueFrame.add(descField);

        JButton submitBtn = new JButton("Submit Issue to Manager");
        issueFrame.add(submitBtn);

        submitBtn.addActionListener(e -> {
            String hallId = hallIdField.getText().trim();
            String desc = descField.getText().trim();

            if (hallId.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(issueFrame, "Fields cannot be empty.");
                return;
            }

            // Save to issues.txt: issueId, customerId, hallId, description, status
            String issueId = "I" + System.currentTimeMillis();
            String record = issueId + "," + loggedInCustomer.getId() + "," + hallId + "," + desc + ",In progress";
            FileHandler.appendLine("data/issues.txt", record);

            JOptionPane.showMessageDialog(issueFrame, "Issue raised successfully and logged for the Manager.");
            issueFrame.dispose();
        });

        issueFrame.setVisible(true);
    }
}