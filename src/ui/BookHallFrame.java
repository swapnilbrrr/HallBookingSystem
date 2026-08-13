package ui;

import models.Customer;
import models.Hall;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;
import services.BookingService;

public class BookHallFrame extends JFrame {
    private JTable hallTable;
    private DefaultTableModel tableModel;
    private Customer customer;

    public BookHallFrame(Customer customer) {
        this.customer = customer;
        setTitle("Book a Hall");
        setSize(600, 400);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());

        // Setup the Table structure
        String[] columns = {"ID", "Name", "Type", "Capacity", "Rate/Hour (RM)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent user from altering text in the UI
            }
        };
        
        hallTable = new JTable(tableModel);
        hallTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(hallTable);
        add(scrollPane, BorderLayout.CENTER);

        // Populate table from data/halls.txt
        loadHalls();

        // Bottom Panel for actions
        JPanel bottomPanel = new JPanel();
        JButton proceedBtn = new JButton("Proceed to Booking");
        JButton cancelBtn = new JButton("Close");

        bottomPanel.add(proceedBtn);
        bottomPanel.add(cancelBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Close button logic
        cancelBtn.addActionListener(e -> this.dispose());

        // Proceed button logic
        proceedBtn.addActionListener(e -> {
            int selectedRow = hallTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a hall from the list first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Extract data from the selected row
            String selectedHallId = (String) tableModel.getValueAt(selectedRow, 0);
            String selectedHallName = (String) tableModel.getValueAt(selectedRow, 1);
            String rate = (String) tableModel.getValueAt(selectedRow, 4);
            
            // Route to the payment frame and close the current window
            new PaymentFrame(customer, selectedHallId, selectedHallName, rate).setVisible(true);
            this.dispose();
        });
    }

    private void loadHalls() {
        BookingService bookingService = new BookingService();
        List<Hall> halls = bookingService.getAllHalls();

        for (Hall hall : halls) {
            Object[] row = {
                hall.getId(), 
                hall.getName(), 
                hall.getType(), 
                hall.getCapacity(), 
                String.format("%.2f", hall.getRatePerHour())
            };
            tableModel.addRow(row);
        }
    }
}