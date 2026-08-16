package ui.admin;

import models.Booking;
import models.User;
import models.enums.BookingStatus;
import models.enums.Timeframe;
import services.BookingService;
import services.UserService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Booking Management: view and filter upcoming and past bookings for all customers.
 */
public class AllBookingsFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final BookingService bookings = new BookingService();
    private final UserService users = new UserService();
    private final TablePanel<Booking> table;

    private final JComboBox<Timeframe> timeframeFilter = new JComboBox<>(Timeframe.values());
    private final JComboBox<Object> statusFilter =
            new JComboBox<>(UiUtils.anyOption("All statuses", BookingStatus.values()));
    private final JTextField searchField = new JTextField(14);
    private final JLabel summaryLabel = new JLabel();

    private Map<String, String> hallNames;
    private Map<String, String> customerNames;

    public AllBookingsFrame() {
        super("Booking Management", 980, 540);
        this.hallNames = bookings.hallNamesById();
        this.customerNames = customerNames();

        add(UiUtils.header("All bookings",
                "Every booking made by every customer, past and upcoming."),
                BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"Booking ID", "Customer", "Hall", "Date", "Time", "Hours",
                        "Total (RM)", "Status", "When"},
                booking -> new Object[]{
                        booking.getId(),
                        customerNames.getOrDefault(booking.getCustomerId(),
                                booking.getCustomerId()),
                        hallNames.getOrDefault(booking.getHallId(), booking.getHallId()),
                        DateUtil.displayDate(booking.getStart()),
                        DateUtil.displayTime(booking.getStart()) + " - "
                                + DateUtil.displayTime(booking.getEnd()),
                        booking.getHours(),
                        String.format("%.2f", booking.getTotalCost()),
                        booking.getStatus().getLabel(),
                        booking.isPast() ? "Past" : "Upcoming"});
        table.setColumnWidth(1, 140);
        table.setColumnWidth(2, 150);

        timeframeFilter.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> refresh());

        JPanel filters = UiUtils.filterRow(
                new JLabel("Show:"), timeframeFilter,
                new JLabel("Status:"), statusFilter,
                new JLabel("Search:"), searchField,
                UiUtils.button("Apply", this::refresh),
                UiUtils.button("Reset", this::reset));

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(filters, BorderLayout.NORTH);
        centre.add(table, BorderLayout.CENTER);
        centre.add(summaryLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        add(UiUtils.buttonRow(
                UiUtils.button("Refresh", this::refresh),
                UiUtils.button("Close", this::dispose)), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        hallNames = bookings.hallNamesById();
        customerNames = customerNames();

        List<Booking> rows = bookings.filter(
                null, // every customer
                (Timeframe) timeframeFilter.getSelectedItem(),
                UiUtils.selected(statusFilter, BookingStatus.class),
                searchField.getText());
        table.setRows(rows);

        double revenue = 0d;
        int cancelled = 0;
        for (Booking booking : rows) {
            if (booking.isActive()) {
                revenue += booking.getTotalCost();
            } else {
                cancelled++;
            }
        }
        summaryLabel.setText(rows.size() + " booking(s) shown, " + cancelled
                + " cancelled. Value of live bookings: " + UiUtils.money(revenue) + ".");
    }

    private void reset() {
        timeframeFilter.setSelectedItem(Timeframe.ALL);
        statusFilter.setSelectedIndex(0);
        searchField.setText("");
        refresh();
    }

    private Map<String, String> customerNames() {
        Map<String, String> names = new HashMap<>();
        for (User user : users.findAll()) {
            names.put(user.getId(), user.getDisplayName());
        }
        return names;
    }
}
