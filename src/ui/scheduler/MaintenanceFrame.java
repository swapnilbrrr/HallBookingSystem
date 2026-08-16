package ui.scheduler;

import models.Hall;
import models.Maintenance;
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
 * Hall maintenance schedule: set the start and end of each maintenance operation,
 * with optional remarks such as "Air conditioning check and lighting repairs".
 *
 * A hall cannot be taken down while it is already booked, and no booking can be
 * made during a maintenance window.
 */
public class MaintenanceFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final ScheduleService schedule = new ScheduleService();
    private final HallService halls = new HallService();
    private final TablePanel<Maintenance> table;

    private final JComboBox<Object> hallFilter;
    private final JLabel summaryLabel = new JLabel();
    private Map<String, String> hallNames;

    public MaintenanceFrame() {
        super("Hall Maintenance Schedule", 900, 520);
        this.hallNames = hallNames();

        add(UiUtils.header("Hall maintenance schedule",
                "No bookings can be taken while a hall is under maintenance."),
                BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"ID", "Hall", "Starts", "Ends", "Hours", "Remarks", "State"},
                slot -> new Object[]{
                        slot.getId(),
                        hallNames.getOrDefault(slot.getHallId(), slot.getHallId()),
                        DateUtil.display(slot.getStart()),
                        DateUtil.display(slot.getEnd()),
                        slot.getDurationHours(),
                        UiUtils.orDash(slot.getRemarks()),
                        slot.isPast() ? "Completed" : "Scheduled"});
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

        JButton add = UiUtils.button("Add Maintenance", this::addWindow);
        JButton edit = UiUtils.button("Edit Maintenance", this::editWindow);
        JButton delete = UiUtils.button("Delete Maintenance", this::deleteWindow);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, delete, edit, add), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        hallNames = hallNames();
        Hall selectedHall = UiUtils.selected(hallFilter, Hall.class);
        List<Maintenance> rows = selectedHall == null
                ? schedule.findAllMaintenance()
                : schedule.findMaintenanceByHall(selectedHall.getId());
        table.setRows(rows);
        summaryLabel.setText(rows.size() + " maintenance window(s) shown.");
    }

    private void addWindow() {
        List<Hall> options = halls.findAll();
        if (options.isEmpty()) {
            UiUtils.error(this, "There are no halls to schedule maintenance for.");
            return;
        }
        new TimeSlotDialog(this, "Maintenance", options, null,
                (hallId, startDate, startTime, endDate, endTime, remarks) ->
                        schedule.addMaintenance(hallId, startDate, startTime, endDate,
                                endTime, remarks)).setVisible(true);
        refresh();
    }

    private void editWindow() {
        Maintenance selected = requireSelection("edit");
        if (selected == null) {
            return;
        }
        new TimeSlotDialog(this, "Maintenance", halls.findAll(), selected,
                (hallId, startDate, startTime, endDate, endTime, remarks) ->
                        schedule.updateMaintenance(selected, hallId, startDate, startTime,
                                endDate, endTime, remarks)).setVisible(true);
        refresh();
    }

    private void deleteWindow() {
        Maintenance selected = requireSelection("delete");
        if (selected == null) {
            return;
        }
        if (!UiUtils.confirm(this, "Delete maintenance window " + selected.getId()
                + " (" + selected.getDisplayRange() + ")?")) {
            return;
        }
        if (UiUtils.guarded(this, () -> schedule.deleteMaintenance(selected.getId()))) {
            refresh();
        }
    }

    private Maintenance requireSelection(String action) {
        Maintenance selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select a maintenance window to " + action + ".");
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
