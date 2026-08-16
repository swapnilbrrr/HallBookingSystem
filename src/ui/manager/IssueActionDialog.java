package ui.manager;

import models.Issue;
import models.User;
import models.enums.IssueStatus;
import services.IssueService;
import ui.components.FormPanel;
import ui.components.UiUtils;
import utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

/**
 * The manager's three actions on one issue, each applied independently so a reply
 * can be saved without also changing the status.
 *
 * The status list shows what each state means, using the descriptions given in the
 * brief.
 */
public class IssueActionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Issue issue;
    private final IssueService issues;

    private final JTextArea responseArea;
    private final JComboBox<User> schedulerCombo;
    private final JComboBox<IssueStatus> statusCombo;

    public IssueActionDialog(Window owner, Issue issue, IssueService issues) {
        super(owner, "Issue " + issue.getId(), ModalityType.APPLICATION_MODAL);
        this.issue = issue;
        this.issues = issues;

        setLayout(new BorderLayout());
        add(UiUtils.header("Issue " + issue.getId(),
                "Raised on " + DateUtil.display(issue.getCreatedAt())), BorderLayout.NORTH);

        FormPanel details = new FormPanel();
        details.addReadOnly("Customer:", issue.getCustomerId());
        details.addReadOnly("Hall:", issue.getHallId());
        details.addReadOnly("Booking:", issue.getBookingId());
        details.addReadOnly("Current status:", issue.getStatus().getLabel());
        details.addReadOnly("Description:", issue.getDescription());

        FormPanel actions = new FormPanel();
        responseArea = actions.addTextArea("Response to customer:", issue.getResponse(), 3);

        List<User> schedulers = issues.assignableSchedulers();
        schedulerCombo = actions.addComboBox("Assign scheduler:",
                schedulers.toArray(new User[0]), currentAssignee(schedulers));

        statusCombo = actions.addComboBox("Set status:", IssueStatus.values(),
                issue.getStatus());
        actions.addNote(statusMeanings());

        JPanel detailsSection = new JPanel(new BorderLayout());
        detailsSection.setBorder(BorderFactory.createTitledBorder("Issue details"));
        detailsSection.add(details, BorderLayout.CENTER);

        JPanel actionsSection = new JPanel(new BorderLayout());
        actionsSection.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionsSection.add(actions, BorderLayout.CENTER);

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(detailsSection, BorderLayout.NORTH);
        centre.add(actionsSection, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        JButton saveResponse = UiUtils.button("Save Response", this::saveResponse);
        JButton assign = UiUtils.button("Assign", this::assign);
        JButton updateStatus = UiUtils.button("Update Status", this::updateStatus);
        JButton close = UiUtils.button("Close", this::dispose);
        add(UiUtils.buttonRow(close, updateStatus, assign, saveResponse), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private User currentAssignee(List<User> schedulers) {
        for (User scheduler : schedulers) {
            if (scheduler.getId().equals(issue.getAssignedSchedulerId())) {
                return scheduler;
            }
        }
        return null;
    }

    private void saveResponse() {
        if (UiUtils.guarded(this, () -> issues.respond(issue, responseArea.getText()))) {
            UiUtils.info(this, "Response saved.");
        }
    }

    private void assign() {
        User scheduler = (User) schedulerCombo.getSelectedItem();
        if (scheduler == null) {
            UiUtils.error(this, "There are no active schedulers to assign.");
            return;
        }
        if (UiUtils.guarded(this, () -> issues.assign(issue, scheduler.getId()))) {
            UiUtils.info(this, "Assigned to " + scheduler.getDisplayName() + ".");
        }
    }

    private void updateStatus() {
        IssueStatus status = (IssueStatus) statusCombo.getSelectedItem();
        if (UiUtils.guarded(this, () -> issues.changeStatus(issue, status))) {
            UiUtils.info(this, "Status set to " + status.getLabel() + ".");
        }
    }

    private static String statusMeanings() {
        StringBuilder sb = new StringBuilder("<html>");
        for (IssueStatus status : IssueStatus.values()) {
            sb.append("<b>").append(status.getLabel()).append("</b> - ")
                    .append(status.getDescription()).append("<br>");
        }
        return sb.append("</html>").toString();
    }
}
