package services;

import models.User;
import utils.FileHandler;
import java.util.List;
import models.Customer;

public class AuthService {
    private static final String USER_FILE = "data/users.txt";

    public static User authenticate(String username, String password) {
        List<String> users = FileHandler.readLines(USER_FILE);
        for (String userLine : users) {
            String[] parts = userLine.split(",");
            if (parts.length == 4) {
                String id = parts[0].trim();
                String storedUsername = parts[1].trim();
                String storedPassword = parts[2].trim();
                String role = parts[3];

                if (storedUsername.equals(username) && storedPassword.equals(password)) {
                   if (role.equalsIgnoreCase("Customer")) {
                        return new models.Customer(id, username, password);
                    } else if (role.equalsIgnoreCase("Administrator")) {
                        return new models.Administrator(id, username, password);
                    } else if (role.equalsIgnoreCase("Scheduler")) {
                        return new models.Scheduler(id, username, password);
                    } else if (role.equalsIgnoreCase("Manager")) {
                        return new models.Manager(id, username, password);
                    }// TODO: Add else-if statements for Administrator, Scheduler, and Manager here later                
                    System.out.println("Login successful for user: " + username + " with role: " + role);
                }
            }
        }
        return null; // Authentication failed
    }
    
public boolean registerCustomer(String username, String password) {
        List<String> usersData = FileHandler.readLines(USER_FILE);
        
        // Basic check to prevent duplicate usernames
        for (String line : usersData) {
            // Guard clause: Skip empty lines or malformed data
            if (line == null || line.trim().isEmpty()) {
                continue; 
            }

            String[] data = line.split(",");
            // Ensure the line has at least enough columns to check the username
            if (data.length >= 2) { 
                if (data[1].trim().equalsIgnoreCase(username)) {
                    return false; // Username exists
                }
            }
        }

        // Generate a simple ID (e.g., U005)
        String newId = "U" + String.format("%03d", usersData.size() + 1);
        String newUserRecord = newId + "," + username + "," + password + ",Customer";
        
        FileHandler.appendLine(USER_FILE, newUserRecord);
        return true;
    }

    public void updateCustomerPassword(String userId, String newPassword) {
        List<String> usersData = FileHandler.readLines(USER_FILE);
        List<String> updatedData = new java.util.ArrayList<>();

        for (String line : usersData) {
            String[] data = line.split(",");
            if (data[0].trim().equals(userId)) {
                // Rebuild the line with the new password
                updatedData.add(data[0] + "," + data[1] + "," + newPassword + "," + data[3]);
            } else {
                updatedData.add(line);
            }
        }
        FileHandler.writeAllLines(USER_FILE, updatedData);
    }
    
}