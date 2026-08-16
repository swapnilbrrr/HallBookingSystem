package ui.manager;

import models.Issue;
import models.enums.IssueStatus;
import services.IssueService;
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
 * Maintenance operation: view and respond to customer issues, assign a scheduler to
 * fix them, and move them between the four statuses.
 */
public class IssueManagementFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final IssueService issues = new IssueService();
    private final TablePanel<Issue> table;

    private final JComboBox<Object> statusFilter =
            new JComboBox<>(UiUtils.anyOption("All statuses", IssueStatus.values()));
    private final JTextField searchField = new JTextField(14);
    private final JLabel summaryLabel = new JLabel();

    private Map<String, String> hallNames;
    private Map<String, String> userNames;

    public IssueManagementFrame() {
        super("Maintenance Operations", 1000, 540);
        this.hallNames = issues.hallNamesById();
        this.userNames = issues.userNamesById();

        add(UiUtils.header("Customer issues",
                "Respond, assign a scheduler, and update the status."), BorderLayout.NORTH);

        table = new TablePanel<>(
                new String[]{"Issue ID", "Raised on", "Customer", "Hall", "Booking",
                        "Description", "Status", "Assigned to", "Response"},
                issue -> new Object[]{
                        issue.getId(),
                        DateUtil.displayDate(issue.getCreatedAt()),
                        userNames.getOrDefault(issue.getCustomerId(), issue.getCustomerId()),
                        hallNames.getOrDefault(issue.getHallId(), issue.getHallId()),
                        UiUtils.orDash(issue.getBookingId()),
                        issue.getDescription(),
                        issue.getStatus().getLabel(),
                        issue.isAssigned()
                                ? userNames.getOrDefault(issue.getAssignedSchedulerId(),
                                        issue.getAssignedSchedulerId())
                                : "-",
                        UiUtils.orDash(issue.getResponse())});
        table.setColumnWidth(5, 210);
        table.setColumnWidth(8, 190);

        statusFilter.addActionListener(e -> refresh());

        JPanel filters = UiUtils.filterRow(
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

        JButton handle = UiUtils.button("Respond / Assign / Update Status", this::handleIssue);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, handle), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        hallNames = issues.hallNamesById();
        userNames = issues.userNamesById();

        List<Issue> rows = issues.filter(searchField.getText(),
                UiUtils.selected(statusFilter, IssueStatus.class));
        table.setRows(rows);

        int open = 0;
        int unassigned = 0;
        for (Issue issue : rows) {
            if (issue.isOpen()) {
                open++;
            }
            if (!issue.isAssigned()) {
                unassigned++;
            }
        }
        summaryLabel.setText(rows.size() + " issue(s) shown, " + open + " still open, "
                + unassigned + " not yet assigned.");
    }

    private void reset() {
        statusFilter.setSelectedIndex(0);
        searchField.setText("");
        refresh();
    }

    private void handleIssue() {
        Issue selected = table.getSelected();
        if (selected == null) {
            UiUtils.error(this, "Please select an issue to work on.");
            return;
        }
        new IssueActionDialog(this, selected, issues).setVisible(true);
        refresh();
    }
}
