package ui.customer;

import models.Customer;
import models.Hall;
import services.BookingService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;
import utils.DateUtil;
import utils.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Step one of booking: choose a slot, see which halls are free for it, and pick one.
 *
 * The list only ever contains halls a scheduler has opened for that period, with
 * maintenance windows and existing bookings already excluded.
 */
public class BookHallFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private static final int MAX_HOURS = 10;

    private final Customer customer;
    private final BookingService bookings = new BookingService();
    private final TablePanel<Hall> table;

    private final JTextField dateField = new JTextField(10);
    private final JTextField timeField = new JTextField(6);
    private final JTextField hoursField = new JTextField(4);
    private final JLabel statusLabel = new JLabel();

    public BookHallFrame(Customer customer) {
        super("Book a Hall", 820, 500);
        this.customer = customer;

        add(UiUtils.header("Available halls",
                "Opening hours are " + DateUtil.operatingHoursText()
                        + ". Search a slot to see which halls are free."), BorderLayout.NORTH);

        dateField.setText(DateUtil.displayDate(LocalDate.now().plusDays(7)));
        timeField.setText("09:00");
        hoursField.setText("3");

        table = new TablePanel<>(
                new String[]{"Hall ID", "Name", "Type", "Capacity", "Rate/Hour (RM)",
                        "Estimated Total (RM)"},
                hall -> new Object[]{
                        hall.getId(),
                        hall.getName(),
                        hall.getTypeLabel(),
                        hall.getCapacity(),
                        String.format("%.2f", hall.getRatePerHour()),
                        estimatedTotal(hall)});
        table.setColumnWidth(1, 180);

        JPanel search = UiUtils.filterRow(
                new JLabel("Date (DD-MM-YYYY):"), dateField,
                new JLabel("Start (HH:MM):"), timeField,
                new JLabel("Hours:"), hoursField,
                UiUtils.button("Search Available Halls", this::searchAvailable),
                UiUtils.button("Show All Halls", this::showAllHalls));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(search, BorderLayout.NORTH);
        centre.add(table, BorderLayout.CENTER);
        centre.add(statusLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        JButton proceed = UiUtils.button("Proceed to Payment", this::proceed);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, proceed), BorderLayout.SOUTH);

        showAllHalls();
    }

    private void showAllHalls() {
        List<Hall> all = bookings.findBookableHalls();
        table.setRows(all);
        statusLabel.setText("Showing all " + all.size()
                + " hall(s) on offer. Search a slot to check availability.");
    }

    private void searchAvailable() {
        UiUtils.guarded(this, () -> {
            LocalDateTime start = requestedStart();
            LocalDateTime end = start.plusHours(requestedHours());

            if (!DateUtil.withinOperatingHours(start, end)) {
                Validator.fail("Bookings must fall within opening hours ("
                        + DateUtil.operatingHoursText() + ") on a single day.");
            }

            List<Hall> free = bookings.findHallsAvailableFor(start, end);
            table.setRows(free);
            statusLabel.setText(free.isEmpty()
                    ? "No halls are free for " + DateUtil.displayRange(start, end)
                            + ". Try another date, time or duration."
                    : free.size() + " hall(s) free for "
                            + DateUtil.displayRange(start, end) + ".");
        });
    }

    private void proceed() {
        Hall selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select a hall from the list first.");
            return;
        }
        UiUtils.guarded(this, () -> {
            // Parsed here only to fail early with a clear message; BookingService
            // validates everything again before it stores anything.
            requestedStart();
            requestedHours();

            new PaymentFrame(customer, selected, dateField.getText(), timeField.getText(),
                    hoursField.getText()).setVisible(true);
            dispose();
        });
    }

    private LocalDateTime requestedStart() {
        LocalDate date = DateUtil.parseInputDate(dateField.getText(), "Booking date");
        LocalTime time = DateUtil.parseInputTime(timeField.getText(), "Start time");
        return LocalDateTime.of(date, time);
    }

    private int requestedHours() {
        return Validator.intInRange(hoursField.getText(), "Hours", 1, MAX_HOURS);
    }

    /** Cost for the currently typed duration, or a dash when it is not a number yet. */
    private String estimatedTotal(Hall hall) {
        try {
            int hours = Integer.parseInt(hoursField.getText().trim());
            return hours > 0 ? String.format("%.2f", hall.costFor(hours)) : "-";
        } catch (NumberFormatException ex) {
            return "-";
        }
    }
}
