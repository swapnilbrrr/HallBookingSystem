package ui.components;

import utils.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/** Small helpers shared by every screen, so dialogs and headers look the same. */
public final class UiUtils {

    public static final String APP_NAME = "Hall Symphony Inc.";

    private UiUtils() {
    }

    // ---------- message boxes ----------

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, APP_NAME,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Runs an action, turning a validation failure into a dialog box.
     *
     * Every form uses this, so the rules stay in the service layer and no screen
     * needs its own error handling.
     *
     * @return true when the action completed without complaint
     */
    public static boolean guarded(Component parent, Runnable action) {
        try {
            action.run();
            return true;
        } catch (Validator.ValidationException ex) {
            error(parent, ex.getMessage());
            return false;
        } catch (RuntimeException ex) {
            error(parent, "Something went wrong: " + ex);
            return false;
        }
    }

    // ---------- layout ----------

    /** Title and subtitle block used at the top of each screen. */
    public static JPanel header(String title, String subtitle) {
        JPanel panel = new JPanel(new GridLayout(subtitle == null ? 1 : 2, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 17f));
        panel.add(titleLabel);

        if (subtitle != null) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
            panel.add(subtitleLabel);
        }
        return panel;
    }

    /** Right-aligned row of buttons for the bottom of a screen. */
    public static JPanel buttonRow(JComponent... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        for (JComponent button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    /** Left-aligned row, used for filter bars. */
    public static JPanel filterRow(JComponent... fields) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        for (JComponent field : fields) {
            panel.add(field);
        }
        return panel;
    }

    public static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    // ---------- formatting ----------

    public static String money(double amount) {
        return String.format("RM %,.2f", amount);
    }

    /** Falls back to a dash so tables never show an empty cell. */
    public static String orDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    // ---------- filter combo boxes ----------

    /**
     * Prepends an "any value" label to a set of enum constants.
     *
     * Filter drop-downs need a "no restriction" choice that the enum itself does
     * not define; pairing this with {@link #selected} keeps that out of the enums.
     */
    public static Object[] anyOption(String anyLabel, Object[] values) {
        Object[] options = new Object[values.length + 1];
        options[0] = anyLabel;
        System.arraycopy(values, 0, options, 1, values.length);
        return options;
    }

    /** The selected item when it is of the given type, otherwise null ("any"). */
    @SuppressWarnings("unchecked")
    public static <E> E selected(javax.swing.JComboBox<?> combo, Class<E> type) {
        Object value = combo.getSelectedItem();
        return type.isInstance(value) ? (E) value : null;
    }
}
