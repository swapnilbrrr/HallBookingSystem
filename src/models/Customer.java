package models;

import ui.CustomerDashboard;

public class Customer extends User {
    
    public Customer(String id, String username, String password) {
        super(id, username, password, "Customer");
    }

    @Override
    public void showDashboard() {
        CustomerDashboard dashboard = new CustomerDashboard(this);
        dashboard.setVisible(true);
    }
}