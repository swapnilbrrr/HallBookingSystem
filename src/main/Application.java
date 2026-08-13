package main;

import ui.LoginFrame; // <-- This must be exactly this
import javax.swing.SwingUtilities;
import java.io.File;

public class Application {
    public static void main(String[] args) {
        
        // Ensure data directory exists before starting
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        // Run GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}