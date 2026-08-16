package main;

import repository.AvailabilityRepository;
import repository.BookingRepository;
import repository.HallRepository;
import repository.IssueRepository;
import repository.MaintenanceRepository;
import repository.UserRepository;
import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;

/**
 * Program entry point for the Hall Booking Management System.
 *
 * All data lives in plain .txt files under {@code data/}, as the brief requires.
 */
public class Application {

    public static void main(String[] args) {
        new File("data").mkdirs();

        // Touching each repository creates any missing data file with its
        // documenting header, so a fresh checkout starts in a usable state.
        new UserRepository();
        new HallRepository();
        new AvailabilityRepository();
        new MaintenanceRepository();
        new BookingRepository();
        new IssueRepository();

        useSystemLookAndFeel();

        // Swing components must only be touched on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // The cross-platform default is perfectly usable, so carry on.
            System.err.println("Falling back to the default look and feel: " + ex.getMessage());
        }
    }
}
