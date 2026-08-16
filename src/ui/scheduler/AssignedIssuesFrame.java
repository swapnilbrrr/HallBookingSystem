package ui.scheduler;

import models.Issue;
import models.Scheduler;
import services.IssueService;
import ui.components.BaseFrame;
import ui.components.TablePanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read-only list of the issues a manager has assigned to this scheduler.
 *
 * Status changes stay with the manager, as the brief specifies; this screen simply
 * lets a scheduler see the work waiting for them.
 */
public class AssignedIssuesFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Scheduler scheduler;
    private final IssueService issues = new IssueService();
    private final TablePanel<Issue> table;
    private final JLabel summaryLabel = new JLabel();

    public AssignedIssuesFrame(Scheduler scheduler) {
        super("My Assigned Issues", 900, 480);
        this.scheduler = scheduler;

        add(UiUtils.header("Issues assigned to me",
                "Raised by customers and assigned by the manager."), BorderLayout.NORTH);

        Map<String, String> hallNames = issues.hallNamesById();
        Map<String, String> userNames = issues.userNamesById();

        table = new TablePanel<>(
                new String[]{"Issue ID", "Hall", "Raised by", "Raised on", "Description",
                        "Status", "Manager response"},
                issue -> new Object[]{
                        issue.getId(),
                        hallNames.getOrDefault(issue.getHallId(), issue.getHallId()),
                        userNames.getOrDefault(issue.getCustomerId(), issue.getCustomerId()),
                        DateUtil.displayDate(issue.getCreatedAt()),
                        issue.getDescription(),
                        issue.getStatus().getLabel(),
                        UiUtils.orDash(issue.getResponse())});
        table.setColumnWidth(4, 220);
        table.setColumnWidth(6, 200);

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(table, BorderLayout.CENTER);
        centre.add(summaryLabel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        add(UiUtils.buttonRow(
                UiUtils.button("Refresh", this::refresh),
                UiUtils.button("Close", this::dispose)), BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        List<Issue> mine = new ArrayList<>();
        int open = 0;
        for (Issue issue : issues.findAll()) {
            if (scheduler.getId().equals(issue.getAssignedSchedulerId())) {
                mine.add(issue);
                if (issue.isOpen()) {
                    open++;
                }
            }
        }
        table.setRows(mine);
        summaryLabel.setText(mine.size() + " issue(s) assigned to you, " + open + " still open.");
    }
}
