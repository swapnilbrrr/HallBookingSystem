package ui.components;

import javax.swing.JFrame;
import java.awt.BorderLayout;

/**
 * Common window setup: branded title, size, centring and close behaviour.
 *
 * Child windows dispose themselves; the dashboards raise this to EXIT_ON_CLOSE
 * so that closing the main window ends the program.
 */
public abstract class BaseFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    protected BaseFrame(String title, int width, int height) {
        setTitle(title + " - " + UiUtils.APP_NAME);
        setLayout(new BorderLayout());
        setSize(width, height);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
