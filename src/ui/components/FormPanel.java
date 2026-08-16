package ui.components;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;

/**
 * A two-column "label on the left, input on the right" form.
 *
 * Each {@code add...} method appends a row and hands back the control, so a
 * dialog reads as a short list of fields instead of a wall of layout code.
 */
public class FormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private int row = 0;

    public FormPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
    }

    public JTextField addTextField(String label, String initialValue) {
        JTextField field = new JTextField(initialValue == null ? "" : initialValue, 22);
        addRow(label, field);
        return field;
    }

    public JPasswordField addPasswordField(String label) {
        JPasswordField field = new JPasswordField(22);
        addRow(label, field);
        return field;
    }

    public <E> JComboBox<E> addComboBox(String label, E[] items, E selected) {
        JComboBox<E> combo = new JComboBox<>(items);
        if (selected != null) {
            combo.setSelectedItem(selected);
        }
        addRow(label, combo);
        return combo;
    }

    public JTextArea addTextArea(String label, String initialValue, int visibleRows) {
        JTextArea area = new JTextArea(initialValue == null ? "" : initialValue, visibleRows, 22);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        addRow(label, new JScrollPane(area));
        return area;
    }

    /** Read-only row for values the user may see but not change. */
    public JLabel addReadOnly(String label, String value) {
        JLabel valueLabel = new JLabel(UiUtils.orDash(value));
        addRow(label, valueLabel);
        return valueLabel;
    }

    /** Small italic note spanning both columns, for hints and rules. */
    public void addNote(String text) {
        JLabel note = new JLabel(text);
        note.setFont(note.getFont().deriveFont(Font.ITALIC, 11.5f));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 4, 4, 4);
        add(note, c);
    }

    public void addRow(String label, JComponent field) {
        GridBagConstraints labelSpot = new GridBagConstraints();
        labelSpot.gridx = 0;
        labelSpot.gridy = row;
        labelSpot.anchor = GridBagConstraints.NORTHWEST;
        labelSpot.insets = new Insets(5, 4, 5, 10);
        add(new JLabel(label), labelSpot);

        GridBagConstraints fieldSpot = new GridBagConstraints();
        fieldSpot.gridx = 1;
        fieldSpot.gridy = row;
        fieldSpot.weightx = 1.0;
        fieldSpot.fill = GridBagConstraints.HORIZONTAL;
        fieldSpot.anchor = GridBagConstraints.WEST;
        fieldSpot.insets = new Insets(5, 0, 5, 4);
        add(field, fieldSpot);

        row++;
    }
}
