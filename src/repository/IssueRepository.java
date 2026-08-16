package repository;

import models.Issue;
import models.enums.IssueStatus;
import utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/** Store for customer issues raised with the manager. */
public class IssueRepository extends Repository<Issue> {

    public static final String FILE = "data/issues.txt";

    public IssueRepository() {
        super(FILE,
                "# issues.txt",
                "# Format: id|customerId|hallId|bookingId|description|status"
                        + "|assignedSchedulerId|response|createdAt",
                "# status: IN_PROGRESS | DONE | CLOSED | CANCELLED");
    }

    @Override
    protected Issue fromFields(String[] fields) {
        return new Issue(
                at(fields, 0),
                at(fields, 1),
                at(fields, 2),
                at(fields, 3),
                at(fields, 4),
                IssueStatus.fromStorage(at(fields, 5, IssueStatus.IN_PROGRESS.name())),
                at(fields, 6),
                at(fields, 7),
                DateUtil.fromStorageOrNull(at(fields, 8)));
    }

    public List<Issue> findByCustomer(String customerId) {
        List<Issue> matches = new ArrayList<>();
        for (Issue issue : findAll()) {
            if (issue.getCustomerId().equals(customerId)) {
                matches.add(issue);
            }
        }
        return matches;
    }

    public List<Issue> findByScheduler(String schedulerId) {
        List<Issue> matches = new ArrayList<>();
        for (Issue issue : findAll()) {
            if (issue.getAssignedSchedulerId().equals(schedulerId)) {
                matches.add(issue);
            }
        }
        return matches;
    }
}
