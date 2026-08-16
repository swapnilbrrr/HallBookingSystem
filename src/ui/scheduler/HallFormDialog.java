package ui.scheduler;

import models.Hall;
import models.enums.HallType;
import ui.components.FormPanel;
import ui.components.UiUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Window;

/**
 * Add or edit one hall.
 *
 * The caller supplies a {@link HallSaver}, so the same dialog serves both cases:
 * the create screen passes a lambda that inserts, the edit screen one that updates.
 */
public class HallFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /** What to do with the entered values. Implemented by a lambda at each call site. */
    public interface HallSaver {
        void save(String name, HallType type, String capacity, String ratePerHour);
    }

    private final HallSaver saver;
    private final JTextField nameField;
    private final JComboBox<HallType> typeCombo;
    private final JTextField capacityField;
    private final JTextField rateField;

    /**
     * @param existing the hall being edited, or null when adding a new one
     */
    public HallFormDialog(Window owner, Hall existing, HallSaver saver) {
        super(owner, existing == null ? "Add Hall" : "Edit Hall " + existing.getId(),
                ModalityType.APPLICATION_MODAL);
        this.saver = saver;

        setLayout(new BorderLayout());
        add(UiUtils.header(existing == null ? "New hall" : "Edit hall",
                "Capacity and rate default to the standard values for the type."),
                BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        nameField = form.addTextField("Hall name:", existing == null ? "" : existing.getName());
        typeCombo = form.addComboBox("Type:", HallType.values(),
                existing == null ? HallType.MEETING_ROOM : existing.getType());
        capacityField = form.addTextField("Capacity (seats):",
                existing == null ? String.valueOf(HallType.MEETING_ROOM.getDefaultCapacity())
                        : String.valueOf(existing.getCapacity()));
        rateField = form.addTextField("Rate per hour (RM):",
                existing == null ? String.format("%.2f", HallType.MEETING_ROOM.getRatePerHour())
                        : String.format("%.2f", existing.getRatePerHour()));
        form.addNote("Standard rates: Auditorium RM 300.00, Banquet Hall RM 100.00, "
                + "Meeting Room RM 50.00.");
        add(form, BorderLayout.CENTER);

        // Choosing a type refills capacity and rate with that type's standard values.
        typeCombo.addActionListener(e -> applyTypeDefaults());

        JButton save = UiUtils.button("Save", this::save);
        JButton cancel = UiUtils.button("Cancel", this::dispose);
        add(UiUtils.buttonRow(cancel, save), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(save);
        pack();
        setLocationRelativeTo(owner);
    }

    private void applyTypeDefaults() {
        HallType type = (HallType) typeCombo.getSelectedItem();
        if (type != null) {
            capacityField.setText(String.valueOf(type.getDefaultCapacity()));
            rateField.setText(String.format("%.2f", type.getRatePerHour()));
        }
    }

    private void save() {
        boolean ok = UiUtils.guarded(this, () -> saver.save(
                nameField.getText(),
                (HallType) typeCombo.getSelectedItem(),
                capacityField.getText(),
                rateField.getText()));
        if (ok) {
            dispose();
        }
    }
}
