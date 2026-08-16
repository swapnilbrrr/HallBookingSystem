package ui.customer;

import models.Booking;
import models.Customer;
import models.Issue;
import services.IssueService;
import ui.components.BaseFrame;
import ui.components.FormPanel;
import ui.components.TablePanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

/**
 * Raise an issue with the manager about a hall this customer booked, and follow
 * the manager's replies.
 *
 * The hall comes from the chosen booking rather than being typed, so an issue can
 * never reference a hall the customer never used.
 */
public class RaiseIssueFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final IssueService issues = new IssueService();
    private final TablePanel<Issue> table;

    private final JComboBox<Booking> bookingChooser;
    private final JTextArea descriptionArea;

    private Map<String, String> hallNames;
    private Map<String, String> staffNames;

    public RaiseIssueFrame(Customer customer) {
        super("Raise an Issue", 860, 560);
        this.customer = customer;
        this.hallNames = issues.hallNamesById();
        this.staffNames = issues.userNamesById();

        add(UiUtils.header("Raise an issue with the manager",
                "Choose one of your bookings and describe the problem."),
                BorderLayout.NORTH);

        List<Booking> eligible = issues.raisableBookings(customer.getId());

        FormPanel form = new FormPanel();
        bookingChooser = form.addComboBox("Booking:",
                eligible.toArray(new Booking[0]), null);
        descriptionArea = form.addTextArea("Describe the issue:", "", 4);
        form.addNote("Up to 300 characters. The manager will reply and may assign a "
                + "scheduler to fix it.");

        table = new TablePanel<>(
                new String[]{"Issue ID", "Hall", "Raised", "Description", "Status",
                        "Assigned to", "Manager response"},
                issue -> new Object[]{
                        issue.getId(),
                        hallNames.getOrDefault(issue.getHallId(), issue.getHallId()),
                        DateUtil.displayDate(issue.getCreatedAt()),
                        issue.getDescription(),
                        issue.getStatus().getLabel(),
                        issue.isAssigned()
                                ? staffNames.getOrDefault(issue.getAssignedSchedulerId(),
                                        issue.getAssignedSchedulerId())
                                : "-",
                        UiUtils.orDash(issue.getResponse())});
        table.setColumnWidth(3, 220);
        table.setColumnWidth(6, 220);

        // Form and section label stack above the table, which takes the growing space.
        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.NORTH);
        top.add(UiUtils.header("My issues", null), BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(table, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        JButton submit = UiUtils.button("Submit Issue", this::submit);
        JButton refresh = UiUtils.button("Refresh", this::refresh);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, refresh, submit), BorderLayout.SOUTH);

        if (eligible.isEmpty()) {
            UiUtils.info(this, "You have no active bookings yet, so there is nothing to "
                    + "raise an issue about.");
        }
        refresh();
    }

    private void submit() {
        Booking booking = (Booking) bookingChooser.getSelectedItem();
        boolean ok = UiUtils.guarded(this,
                () -> issues.raise(customer.getId(), booking, descriptionArea.getText()));
        if (ok) {
            UiUtils.info(this, "Your issue has been logged for the manager.");
            descriptionArea.setText("");
            refresh();
        }
    }

    private void refresh() {
        hallNames = issues.hallNamesById();
        staffNames = issues.userNamesById();
        table.setRows(issues.findByCustomer(customer.getId()));
    }
}
