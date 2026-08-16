package ui.customer;

import models.Customer;
import ui.components.DashboardFrame;

/** Home screen for a customer. */
public class CustomerDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    public CustomerDashboard(Customer customer) {
        super(customer, "Customer Dashboard", "Reserve halls for your events.", 640, 400);

        addAction("View & Book Halls", () -> new BookHallFrame(customer).setVisible(true));
        addAction("My Bookings & Cancel", () -> new MyBookingsFrame(customer).setVisible(true));
        addAction("Raise an Issue", () -> new RaiseIssueFrame(customer).setVisible(true));
        addAction("Update Profile", () -> new UpdateProfileFrame(customer).setVisible(true));
    }
}
