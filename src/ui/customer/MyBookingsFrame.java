package ui.customer;

import models.Booking;
import models.Customer;
import models.Hall;
import models.enums.BookingStatus;
import models.enums.Timeframe;
import services.BookingService;
import services.HallService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

/**
 * "My Bookings": view and filter upcoming and past bookings, reprint a receipt,
 * and cancel subject to the three-day rule.
 */
public class MyBookingsFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final BookingService bookings = new BookingService();
    private final HallService halls = new HallService();
    private final TablePanel<Booking> table;

    private final JComboBox<Timeframe> timeframeFilter =
            new JComboBox<>(Timeframe.values());
    private final JComboBox<Object> statusFilter =
            new JComboBox<>(UiUtils.anyOption("All statuses", BookingStatus.values()));
    private final JTextField searchField = new JTextField(14);
    private final JLabel summaryLabel = new JLabel();

    private Map<String, String> hallNames;

    public MyBookingsFrame(Customer customer) {
        super("My Bookings", 880, 500);
        this.customer = customer;
        this.hallNames = bookings.hallNamesById();

        add(UiUtils.header("My bookings",
                "Cancellation is allowed up to " + DateUtil.MIN_CANCELLATION_DAYS
                        + " days before the booking date."), BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"Booking ID", "Hall", "Date", "Time", "Hours", "Total (RM)",
                        "Status", "When"},
                booking -> new Object[]{
                        booking.getId(),
                        hallLabel(booking),
                        DateUtil.displayDate(booking.getStart()),
                        DateUtil.displayTime(booking.getStart()) + " - "
                                + DateUtil.displayTime(booking.getEnd()),
                        booking.getHours(),
                        String.format("%.2f", booking.getTotalCost()),
                        booking.getStatus().getLabel(),
                        booking.isPast() ? "Past" : "Upcoming"});
        table.setColumnWidth(1, 170);

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

        JButton cancelBooking = UiUtils.button("Cancel Selected Booking", this::cancelSelected);
        JButton viewReceipt = UiUtils.button("View Receipt", this::viewReceipt);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, viewReceipt, cancelBooking), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        hallNames = bookings.hallNamesById();
        List<Booking> rows = bookings.filter(
                customer.getId(),
                (Timeframe) timeframeFilter.getSelectedItem(),
                UiUtils.selected(statusFilter, BookingStatus.class),
                searchField.getText());
        table.setRows(rows);

        double total = 0d;
        int cancellable = 0;
        for (Booking booking : rows) {
            if (booking.isActive()) {
                total += booking.getTotalCost();
            }
            if (booking.isCancellable()) {
                cancellable++;
            }
        }
        summaryLabel.setText(rows.size() + " booking(s) shown. Value of live bookings: "
                + UiUtils.money(total) + ". " + cancellable + " can still be cancelled.");
    }

    private void reset() {
        timeframeFilter.setSelectedItem(Timeframe.ALL);
        statusFilter.setSelectedIndex(0);
        searchField.setText("");
        refresh();
    }

    private void cancelSelected() {
        Booking selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select a booking to cancel.");
            return;
        }
        if (!UiUtils.confirm(this, "Cancel booking " + selected.getId() + " for "
                + hallLabel(selected) + " on " + DateUtil.displayDate(selected.getStart())
                + "?\nThis cannot be undone.")) {
            return;
        }
        if (UiUtils.guarded(this, () -> bookings.cancel(selected))) {
            UiUtils.info(this, "Booking " + selected.getId() + " has been cancelled.");
            refresh();
        }
    }

    private void viewReceipt() {
        Booking selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select a booking to view its receipt.");
            return;
        }
        Hall hall = halls.findById(selected.getHallId()).orElse(null);
        new ReceiptFrame(customer, selected, hall).setVisible(true);
    }

    private String hallLabel(Booking booking) {
        return hallNames.getOrDefault(booking.getHallId(), booking.getHallId());
    }
}
