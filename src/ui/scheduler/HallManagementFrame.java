package ui.scheduler;

import models.Hall;
import models.enums.HallStatus;
import models.enums.HallType;
import services.HallService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Hall Management: add, view and filter, edit, and delete hall information.
 *
 * Deleting withdraws a hall (status INACTIVE) instead of erasing the record, so
 * past bookings keep naming the hall they were for; "Restore" puts it back.
 */
public class HallManagementFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final HallService halls = new HallService();
    private final TablePanel<Hall> table;

    private final JTextField searchField = new JTextField(12);
    private final JComboBox<Object> typeFilter =
            new JComboBox<>(UiUtils.anyOption("All types", HallType.values()));
    private final JComboBox<Object> statusFilter =
            new JComboBox<>(UiUtils.anyOption("All statuses", HallStatus.values()));
    private final JTextField minCapacityField = new JTextField(5);
    private final JLabel summaryLabel = new JLabel();

    public HallManagementFrame() {
        super("Hall Management", 880, 520);

        add(UiUtils.header("Hall management",
                "Add, edit, filter and withdraw the halls on offer."), BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"Hall ID", "Name", "Type", "Capacity", "Rate/Hour (RM)",
                        "Status", "Upcoming bookings"},
                hall -> new Object[]{
                        hall.getId(),
                        hall.getName(),
                        hall.getTypeLabel(),
                        hall.getCapacity(),
                        String.format("%.2f", hall.getRatePerHour()),
                        hall.getStatus().getLabel(),
                        halls.countUpcomingBookings(hall.getId())});
        table.setColumnWidth(1, 180);

        typeFilter.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> refresh());

        JPanel filters = UiUtils.filterRow(
                new JLabel("Search:"), searchField,
                new JLabel("Type:"), typeFilter,
                new JLabel("Status:"), statusFilter,
                new JLabel("Min capacity:"), minCapacityField,
                UiUtils.button("Apply", this::refresh),
                UiUtils.button("Reset", this::reset));

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(filters, BorderLayout.NORTH);
        centre.add(table, BorderLayout.CENTER);
        centre.add(summaryLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        JButton add = UiUtils.button("Add Hall", this::addHall);
        JButton edit = UiUtils.button("Edit Hall", this::editHall);
        JButton delete = UiUtils.button("Delete Hall", this::deleteHall);
        JButton restore = UiUtils.button("Restore Hall", this::restoreHall);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, restore, delete, edit, add), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        int minCapacity = 0;
        String raw = minCapacityField.getText().trim();
        if (!raw.isEmpty()) {
            try {
                minCapacity = Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                minCapacity = 0; // An unparsable filter simply does not restrict.
            }
        }

        List<Hall> rows = halls.filter(
                searchField.getText(),
                UiUtils.selected(typeFilter, HallType.class),
                minCapacity,
                UiUtils.selected(statusFilter, HallStatus.class));
        table.setRows(rows);
        summaryLabel.setText(rows.size() + " hall(s) shown out of "
                + halls.findAll().size() + " in total.");
    }

    private void reset() {
        searchField.setText("");
        minCapacityField.setText("");
        typeFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        refresh();
    }

    private void addHall() {
        new HallFormDialog(this, null, (name, type, capacity, rate) ->
                halls.create(name, type, capacity, rate)).setVisible(true);
        refresh();
    }

    private void editHall() {
        Hall selected = requireSelection("edit");
        if (selected == null) {
            return;
        }
        new HallFormDialog(this, selected, (name, type, capacity, rate) ->
                halls.update(selected, name, type, capacity, rate)).setVisible(true);
        refresh();
    }

    private void deleteHall() {
        Hall selected = requireSelection("delete");
        if (selected == null) {
            return;
        }
        if (!selected.isActive()) {
            UiUtils.error(this, selected.getName() + " has already been withdrawn.");
            return;
        }
        if (!UiUtils.confirm(this, "Withdraw " + selected.getName()
                + " from sale?\nExisting bookings are kept, and you can restore it later.")) {
            return;
        }
        if (UiUtils.guarded(this, () -> halls.delete(selected))) {
            UiUtils.info(this, selected.getName() + " has been withdrawn.");
            refresh();
        }
    }

    private void restoreHall() {
        Hall selected = requireSelection("restore");
        if (selected == null) {
            return;
        }
        if (selected.isActive()) {
            UiUtils.error(this, selected.getName() + " is already on sale.");
            return;
        }
        if (UiUtils.guarded(this, () -> halls.restore(selected))) {
            UiUtils.info(this, selected.getName() + " is on sale again.");
            refresh();
        }
    }

    private Hall requireSelection(String action) {
        Hall selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select a hall to " + action + ".");
        }
        return selected;
    }
}
