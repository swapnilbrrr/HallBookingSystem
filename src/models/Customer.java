package models;

import ui.CustomerDashboard;

public class Customer extends User {
    public Customer(String id, String username, String password) {
        // Must match the roles defined in the assignment
        super(id, username, password, "Customer"); 
    }

    @Override
    public void showDashboard() {
        // Opens the Customer GUI and passes this specific user object to it
        CustomerDashboard dashboard = new CustomerDashboard(this);
        dashboard.setVisible(true);
    }
}