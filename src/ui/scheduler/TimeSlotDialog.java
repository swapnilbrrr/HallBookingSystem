package ui.scheduler;

import models.Hall;
import models.TimeSlot;
import ui.components.FormPanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

/**
 * Add or edit one dated window, used by both the availability and the maintenance
 * screens.
 *
 * The two are identical from the user's point of view - a hall, a start, an end and
 * optional remarks - so one dialog serves both; the caller's {@link SlotSaver}
 * decides which service method receives the values.
 */
public class TimeSlotDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /** Where the entered values go. Supplied as a lambda by each screen. */
    public interface SlotSaver {
        void save(String hallId, String startDate, String startTime, String endDate,
                  String endTime, String remarks);
    }

    private final SlotSaver saver;
    private final JComboBox<Hall> hallCombo;
    private final JTextField startDateField;
    private final JTextField startTimeField;
    private final JTextField endDateField;
    private final JTextField endTimeField;
    private final JTextArea remarksArea;

    /**
     * @param kind     "Availability" or "Maintenance", used in the title and hints
     * @param halls    halls the window may be attached to
     * @param existing the window being edited, or null when adding
     */
    public TimeSlotDialog(Window owner, String kind, List<Hall> halls, TimeSlot existing,
                          SlotSaver saver) {
        super(owner, (existing == null ? "Add " : "Edit ") + kind.toLowerCase()
                + (existing == null ? "" : " " + existing.getId()),
                ModalityType.APPLICATION_MODAL);
        this.saver = saver;

        setLayout(new BorderLayout());
        add(UiUtils.header(existing == null ? "New " + kind.toLowerCase() + " window"
                        : "Edit " + kind.toLowerCase() + " window",
                kind.equals("Maintenance")
                        ? "Bookings cannot be made during a maintenance window."
                        : "Customers can only book inside an availability window."),
                BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        hallCombo = form.addComboBox("Hall:", halls.toArray(new Hall[0]),
                existing == null ? null : findHall(halls, existing.getHallId()));
        startDateField = form.addTextField("Start date (DD-MM-YYYY):",
                existing == null ? "" : DateUtil.displayDate(existing.getStart()));
        startTimeField = form.addTextField("Start time (HH:MM):",
                existing == null ? "08:00" : DateUtil.displayTime(existing.getStart()));
        endDateField = form.addTextField("End date (DD-MM-YYYY):",
                existing == null ? "" : DateUtil.displayDate(existing.getEnd()));
        endTimeField = form.addTextField("End time (HH:MM):",
                existing == null ? "18:00" : DateUtil.displayTime(existing.getEnd()));
        remarksArea = form.addTextArea("Remarks (optional):",
                existing == null ? "" : existing.getRemarks(), 3);
        form.addNote("Business hours are " + DateUtil.operatingHoursText()
                + ". Windows for the same hall may not overlap.");
        add(form, BorderLayout.CENTER);

        JButton save = UiUtils.button("Save", this::save);
        JButton cancel = UiUtils.button("Cancel", this::dispose);
        add(UiUtils.buttonRow(cancel, save), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(save);
        pack();
        setLocationRelativeTo(owner);
    }

    private static Hall findHall(List<Hall> halls, String hallId) {
        for (Hall hall : halls) {
            if (hall.getId().equals(hallId)) {
                return hall;
            }
        }
        return null;
    }

    private void save() {
        Hall hall = (Hall) hallCombo.getSelectedItem();
        boolean ok = UiUtils.guarded(this, () -> saver.save(
                hall == null ? null : hall.getId(),
                startDateField.getText(),
                startTimeField.getText(),
                endDateField.getText(),
                endTimeField.getText(),
                remarksArea.getText()));
        if (ok) {
            dispose();
        }
    }
}
