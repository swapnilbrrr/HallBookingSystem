package ui;

import models.Customer;
import utils.FileHandler;
import javax.swing.*;
import java.awt.GridLayout;

public class PaymentFrame extends JFrame {

    public PaymentFrame(Customer customer, String hallId, String hallName, String rate) {
        setTitle("Payment & Checkout");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10)); // Structured grid for clean UI

        // Display selection details
        add(new JLabel("  Booking Hall: " + hallName));
        add(new JLabel("  Rate: RM " + rate + " per hour"));
        
        // Date Input
        JPanel datePanel = new JPanel();
        datePanel.add(new JLabel("Enter Date (DD-MM-YYYY):"));
        JTextField dateField = new JTextField(10);
        datePanel.add(dateField);
        add(datePanel);

        // Hours Input
        JPanel hoursPanel = new JPanel();
        hoursPanel.add(new JLabel("Hours to book:"));
        JTextField hoursField = new JTextField(5);
        hoursPanel.add(hoursField);
        add(hoursPanel);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton payBtn = new JButton("Confirm & Pay");
        JButton cancelBtn = new JButton("Back");
        btnPanel.add(payBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel);

        // Action Listeners
        cancelBtn.addActionListener(e -> {
            this.dispose();
            new BookHallFrame(customer).setVisible(true); // Return to hall list
        });

        payBtn.addActionListener(e -> {
            String date = dateField.getText().trim();
            String hoursStr = hoursField.getText().trim();
            
            // Basic Validation
            if (date.isEmpty() || hoursStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Date and Hours cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int hours;
            try {
                hours = Integer.parseInt(hoursStr);
                if (hours <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number of hours.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Calculate exact total cost
            double totalCost = Double.parseDouble(rate) * hours;

            // Generate unique booking ID and save record
            String bookingId = "B" + System.currentTimeMillis();
            
            // Schema: BookingID, CustomerID, HallID, Date, Hours, TotalCost, Status
            String bookingRecord = bookingId + "," + customer.getId() + "," + hallId + "," + date + "," + hours + "," + totalCost + ",PAID";
            FileHandler.appendLine("data/bookings.txt", bookingRecord);

            // Generate strict text receipt format 
            String receipt = "--- PAYMENT RECEIPT ---\n" +
                             "Booking ID: " + bookingId + "\n" +
                             "Customer: " + customer.getUsername() + "\n" +
                             "Hall: " + hallName + "\n" +
                             "Date: " + date + "\n" +
                             "Hours Booked: " + hours + "\n" +
                             "Total Paid: RM " + String.format("%.2f", totalCost) + "\n" +
                             "Status: PAID in Full.\n" +
                             "-----------------------";
            
            // Display receipt 
            JOptionPane.showMessageDialog(this, receipt, "Transaction Successful", JOptionPane.INFORMATION_MESSAGE);
            
            this.dispose(); 
            // The user is returned back to their dashboard seamlessly
        });
    }
}