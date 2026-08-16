package ui.scheduler;

import models.Availability;
import models.Hall;
import services.HallService;
import services.ScheduleService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hall availability schedule: set the start and end of each window a hall is open
 * for booking, with optional remarks.
 *
 * Customers can only book inside these windows, which is how the brief's
 * "available halls that has been set by the scheduler" is realised.
 */
public class AvailabilityFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final ScheduleService schedule = new ScheduleService();
    private final HallService halls = new HallService();
    private final TablePanel<Availability> table;

    private final JComboBox<Object> hallFilter;
    private final JLabel summaryLabel = new JLabel();
    private Map<String, String> hallNames;

    public AvailabilityFrame() {
        super("Hall Availability Schedule", 900, 520);
        this.hallNames = hallNames();

        add(UiUtils.header("Hall availability schedule",
                "Customers may only book halls inside these windows."), BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"ID", "Hall", "Starts", "Ends", "Hours", "Remarks", "Bookings"},
                slot -> new Object[]{
                        slot.getId(),
                        hallNames.getOrDefault(slot.getHallId(), slot.getHallId()),
                        DateUtil.display(slot.getStart()),
                        DateUtil.display(slot.getEnd()),
                        slot.getDurationHours(),
                        UiUtils.orDash(slot.getRemarks()),
                        schedule.countBookingsInside(slot)});
        table.setColumnWidth(1, 150);
        table.setColumnWidth(5, 240);

        List<Object> hallOptions = new ArrayList<>();
        hallOptions.add("All halls");
        hallOptions.addAll(halls.findAll());
        hallFilter = new JComboBox<>(hallOptions.toArray());
        hallFilter.addActionListener(e -> refresh());

        JPanel filters = UiUtils.filterRow(
                new JLabel("Hall:"), hallFilter,
                UiUtils.button("Refresh", this::refresh));

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(filters, BorderLayout.NORTH);
        centre.add(table, BorderLayout.CENTER);
        centre.add(summaryLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        JButton add = UiUtils.button("Add Window", this::addWindow);
        JButton edit = UiUtils.button("Edit Window", this::editWindow);
        JButton delete = UiUtils.button("Delete Window", this::deleteWindow);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, delete, edit, add), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        hallNames = hallNames();
        Hall selectedHall = UiUtils.selected(hallFilter, Hall.class);
        List<Availability> rows = selectedHall == null
                ? schedule.findAllAvailability()
                : schedule.findAvailabilityByHall(selectedHall.getId());
        table.setRows(rows);
        summaryLabel.setText(rows.size() + " availability window(s) shown.");
    }

    private void addWindow() {
        List<Hall> options = halls.findActive();
        if (options.isEmpty()) {
            UiUtils.error(this, "There are no active halls to schedule. Add a hall first.");
            return;
        }
        new TimeSlotDialog(this, "Availability", options, null,
                (hallId, startDate, startTime, endDate, endTime, remarks) ->
                        schedule.addAvailability(hallId, startDate, startTime, endDate,
                                endTime, remarks)).setVisible(true);
        refresh();
    }

    private void editWindow() {
        Availability selected = requireSelection("edit");
        if (selected == null) {
            return;
        }
        new TimeSlotDialog(this, "Availability", halls.findAll(), selected,
                (hallId, startDate, startTime, endDate, endTime, remarks) ->
                        schedule.updateAvailability(selected, hallId, startDate, startTime,
                                endDate, endTime, remarks)).setVisible(true);
        refresh();
    }

    private void deleteWindow() {
        Availability selected = requireSelection("delete");
        if (selected == null) {
            return;
        }
        int affected = schedule.countBookingsInside(selected);
        String warning = affected == 0 ? ""
                : "\n\nWarning: " + affected + " existing booking(s) fall inside this window."
                        + " They will be kept, but the hall will no longer be open here.";
        if (!UiUtils.confirm(this, "Delete availability window " + selected.getId()
                + " (" + selected.getDisplayRange() + ")?" + warning)) {
            return;
        }
        if (UiUtils.guarded(this, () -> schedule.deleteAvailability(selected.getId()))) {
            refresh();
        }
    }

    private Availability requireSelection(String action) {
        Availability selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select an availability window to " + action + ".");
        }
        return selected;
    }

    private Map<String, String> hallNames() {
        Map<String, String> names = new HashMap<>();
        for (Hall hall : halls.findAll()) {
            names.put(hall.getId(), hall.getName());
        }
        return names;
    }
}
