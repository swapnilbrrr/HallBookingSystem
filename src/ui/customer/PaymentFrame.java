package ui.customer;

import models.Booking;
import models.Customer;
import models.Hall;
import services.BookingService;
import ui.components.BaseFrame;
import ui.components.FormPanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.JButton;
import java.awt.BorderLayout;

/**
 * Step two of booking: confirm the slot and pay.
 *
 * The booking is only written once {@code BookingService.create} has re-checked
 * every rule, so a stale hall list can never produce a double booking.
 */
public class PaymentFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final Hall hall;
    private final String dateText;
    private final String timeText;
    private final String hoursText;
    private final BookingService bookings = new BookingService();

    public PaymentFrame(Customer customer, Hall hall, String dateText, String timeText,
                        String hoursText) {
        super("Payment & Checkout", 520, 400);
        this.customer = customer;
        this.hall = hall;
        this.dateText = dateText;
        this.timeText = timeText;
        this.hoursText = hoursText;

        add(UiUtils.header("Confirm your booking",
                "Payment is taken in full at the time of booking."), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        form.addReadOnly("Hall:", hall.getName() + " (" + hall.getId() + ")");
        form.addReadOnly("Type:", hall.getTypeLabel() + ", seats " + hall.getCapacity());
        form.addReadOnly("Date:", dateText);
        form.addReadOnly("Start time:", timeText);
        form.addReadOnly("Hours booked:", hoursText);
        form.addReadOnly("Rate per hour:", UiUtils.money(hall.getRatePerHour()));
        form.addReadOnly("Total payable:", UiUtils.money(estimatedCost()));
        form.addNote("Bookings may be cancelled up to "
                + DateUtil.MIN_CANCELLATION_DAYS + " days before the booking date.");
        add(form, BorderLayout.CENTER);

        JButton pay = UiUtils.button("Confirm & Pay", this::confirmAndPay);
        JButton back = UiUtils.button("Back", this::back);
        add(UiUtils.buttonRow(back, pay), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(pay);
    }

    private void confirmAndPay() {
        UiUtils.guarded(this, () -> {
            Booking booking = bookings.create(customer.getId(), hall.getId(), dateText,
                    timeText, hoursText);
            new ReceiptFrame(customer, booking, hall).setVisible(true);
            dispose();
        });
    }

    private void back() {
        dispose();
        new BookHallFrame(customer).setVisible(true);
    }

    private double estimatedCost() {
        try {
            return hall.costFor(Long.parseLong(hoursText.trim()));
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }
}
