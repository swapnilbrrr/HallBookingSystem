package ui.customer;

import models.Booking;
import models.Hall;
import models.User;
import ui.components.BaseFrame;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * The payment receipt, shown in its own window as the brief requires
 * ("view receipt of the payment along with the booking information").
 *
 * Also reachable later from "My Bookings", so a customer can reprint it.
 */
public class ReceiptFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    public ReceiptFrame(User customer, Booking booking, Hall hall) {
        super("Payment Receipt", 470, 430);

        add(UiUtils.header("Payment receipt",
                "Booking reference " + booking.getId()), BorderLayout.NORTH);

        JTextArea receipt = new JTextArea(build(customer, booking, hall));
        receipt.setEditable(false);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receipt.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(new JScrollPane(receipt), BorderLayout.CENTER);

        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close), BorderLayout.SOUTH);
    }

    private static String build(User customer, Booking booking, Hall hall) {
        String hallName = hall == null ? booking.getHallId()
                : hall.getName() + " (" + hall.getId() + ")";
        String hallType = hall == null ? "-" : hall.getTypeLabel();
        String rate = hall == null ? "-" : UiUtils.money(hall.getRatePerHour());

        StringBuilder sb = new StringBuilder();
        sb.append("=================================================\n");
        sb.append("            ").append(UiUtils.APP_NAME).append("\n");
        sb.append("              PAYMENT RECEIPT\n");
        sb.append("=================================================\n\n");
        line(sb, "Booking ID", booking.getId());
        line(sb, "Issued on", DateUtil.display(booking.getCreatedAt()));
        sb.append('\n');
        line(sb, "Customer", customer.getDisplayName());
        line(sb, "Customer ID", customer.getId());
        sb.append('\n');
        line(sb, "Hall", hallName);
        line(sb, "Hall type", hallType);
        line(sb, "Date", DateUtil.displayDate(booking.getStart()));
        line(sb, "Time", DateUtil.displayTime(booking.getStart())
                + " - " + DateUtil.displayTime(booking.getEnd()));
        line(sb, "Hours booked", String.valueOf(booking.getHours()));
        line(sb, "Rate per hour", rate);
        sb.append("\n-------------------------------------------------\n");
        line(sb, "TOTAL PAID", UiUtils.money(booking.getTotalCost()));
        line(sb, "Status", booking.getStatus().getLabel() + " in full");
        sb.append("-------------------------------------------------\n\n");
        sb.append("Cancellation is permitted up to ")
                .append(DateUtil.MIN_CANCELLATION_DAYS)
                .append(" days before the\nbooking date. Thank you for your booking.\n");
        return sb.toString();
    }

    private static void line(StringBuilder sb, String label, String value) {
        sb.append(String.format("  %-16s : %s%n", label, value));
    }
}
