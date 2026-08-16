package ui.manager;

import models.enums.SalesPeriod;
import services.SalesService;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Sales dashboard: view and filter the sales made weekly, monthly and yearly from
 * the bookings, with a per-hall breakdown alongside.
 *
 * Cancelled bookings are excluded, so the totals reflect money actually taken.
 */
public class SalesDashboardFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final SalesService sales = new SalesService();
    private final TablePanel<SalesService.SalesRow> periodTable;
    private final TablePanel<SalesService.SalesRow> hallTable;

    private final JComboBox<SalesPeriod> periodCombo = new JComboBox<>(SalesPeriod.values());
    private final JTextField fromField = new JTextField(10);
    private final JTextField toField = new JTextField(10);
    private final JLabel totalsLabel = new JLabel();

    public SalesDashboardFrame() {
        super("Sales Dashboard", 940, 600);

        add(UiUtils.header("Sales performance",
                "Revenue from paid bookings. Cancelled bookings are excluded."),
                BorderLayout.NORTH);

        periodCombo.setSelectedItem(SalesPeriod.MONTHLY);
        periodCombo.addActionListener(e -> refresh());

        String[] columns = {"Period", "Bookings", "Revenue (RM)", "Average per booking (RM)"};
        periodTable = new TablePanel<>(columns, row -> new Object[]{
                row.getPeriod(),
                row.getBookingCount(),
                String.format("%,.2f", row.getRevenue()),
                String.format("%,.2f", row.getAveragePerBooking())});
        periodTable.setColumnWidth(0, 240);

        hallTable = new TablePanel<>(
                new String[]{"Hall", "Bookings", "Revenue (RM)", "Average per booking (RM)"},
                row -> new Object[]{
                        row.getPeriod(),
                        row.getBookingCount(),
                        String.format("%,.2f", row.getRevenue()),
                        String.format("%,.2f", row.getAveragePerBooking())});
        hallTable.setColumnWidth(0, 240);

        JPanel filters = UiUtils.filterRow(
                new JLabel("Group by:"), periodCombo,
                new JLabel("From (DD-MM-YYYY):"), fromField,
                new JLabel("To (DD-MM-YYYY):"), toField,
                UiUtils.button("Apply", this::refresh),
                UiUtils.button("Clear dates", this::clearDates));

        totalsLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        totalsLabel.setFont(totalsLabel.getFont().deriveFont(java.awt.Font.BOLD, 13f));

        JPanel periodSection = new JPanel(new BorderLayout());
        periodSection.setBorder(BorderFactory.createTitledBorder("By period"));
        periodSection.add(periodTable, BorderLayout.CENTER);

        JPanel hallSection = new JPanel(new BorderLayout());
        hallSection.setBorder(BorderFactory.createTitledBorder("By hall"));
        hallSection.add(hallTable, BorderLayout.CENTER);

        JPanel tables = new JPanel(new java.awt.GridLayout(2, 1, 0, 8));
        tables.add(periodSection);
        tables.add(hallSection);

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(filters, BorderLayout.NORTH);
        centre.add(tables, BorderLayout.CENTER);
        centre.add(totalsLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        add(UiUtils.buttonRow(
                UiUtils.button("Refresh", this::refresh),
                UiUtils.button("Close", this::dispose)), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        UiUtils.guarded(this, () -> {
            LocalDate from = optionalDate(fromField.getText(), "From date");
            LocalDate to = optionalDate(toField.getText(), "To date");
            if (from != null && to != null && to.isBefore(from)) {
                utils.Validator.fail("The 'To' date must not be earlier than the 'From' date.");
            }

            SalesPeriod period = (SalesPeriod) periodCombo.getSelectedItem();
            List<SalesService.SalesRow> rows = sales.summarise(period, from, to);
            periodTable.setRows(rows);
            hallTable.setRows(sales.summariseByHall(from, to));

            int count = sales.totalBookings(from, to);
            double revenue = sales.totalRevenue(from, to);
            totalsLabel.setText("Total: " + count + " paid booking(s), "
                    + UiUtils.money(revenue)
                    + (count == 0 ? "" : "  |  Average " + UiUtils.money(revenue / count)
                            + " per booking")
                    + "  |  " + rows.size() + " " + period.getLabel().toLowerCase()
                    + " period(s)");
        });
    }

    private void clearDates() {
        fromField.setText("");
        toField.setText("");
        refresh();
    }

    /** Blank means "no bound"; anything else must be a valid date. */
    private static LocalDate optionalDate(String text, String fieldName) {
        return text == null || text.trim().isEmpty()
                ? null
                : DateUtil.parseInputDate(text, fieldName);
    }
}
