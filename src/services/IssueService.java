package services;

import models.Booking;
import models.Hall;
import models.Issue;
import models.User;
import models.enums.IssueStatus;
import models.enums.Role;
import models.enums.UserStatus;
import repository.BookingRepository;
import repository.HallRepository;
import repository.IssueRepository;
import repository.UserRepository;
import utils.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Customer issues: raised on the Customer dashboard, worked through on the
 * Manager's "Maintenance operation" screen.
 */
public class IssueService {

    private final IssueRepository issues = new IssueRepository();
    private final BookingRepository bookings = new BookingRepository();
    private final UserRepository users = new UserRepository();
    private final HallRepository halls = new HallRepository();

    // ---------- raising ----------

    /**
     * Records a complaint about a hall the customer booked.
     *
     * The hall is taken from the chosen booking rather than typed in, so an issue
     * can never point at a hall the customer never used.
     */
    public Issue raise(String customerId, Booking booking, String description) {
        if (booking == null) {
            Validator.fail("Please choose the booking the issue relates to.");
        }
        if (!booking.getCustomerId().equals(customerId)) {
            Validator.fail("You may only raise issues about your own bookings.");
        }
        String detail = Validator.maxLength(
                Validator.text(description, "Issue description"), "Issue description", 300);

        Issue issue = new Issue(
                issues.nextId("I"),
                customerId,
                booking.getHallId(),
                booking.getId(),
                detail,
                IssueStatus.IN_PROGRESS,
                "",
                "",
                LocalDateTime.now());
        issues.insert(issue);
        return issue;
    }

    /** Bookings a customer may raise an issue about: their own, not cancelled. */
    public List<Booking> raisableBookings(String customerId) {
        List<Booking> eligible = new ArrayList<>();
        for (Booking booking : bookings.findByCustomer(customerId)) {
            if (booking.isActive()) {
                eligible.add(booking);
            }
        }
        return eligible;
    }

    // ---------- queries ----------

    public List<Issue> findAll() {
        return issues.findAll();
    }

    public List<Issue> findByCustomer(String customerId) {
        return issues.findByCustomer(customerId);
    }

    public Optional<Issue> findById(String id) {
        return issues.findById(id);
    }

    /** Filters by free text and status; null status means "any". */
    public List<Issue> filter(String query, IssueStatus status) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        Map<String, String> hallNames = hallNamesById();
        Map<String, String> userNames = userNamesById();

        List<Issue> matches = new ArrayList<>();
        for (Issue issue : issues.findAll()) {
            if (status != null && issue.getStatus() != status) {
                continue;
            }
            if (!needle.isEmpty()) {
                boolean hit = issue.getId().toLowerCase().contains(needle)
                        || issue.getDescription().toLowerCase().contains(needle)
                        || issue.getHallId().toLowerCase().contains(needle)
                        || issue.getCustomerId().toLowerCase().contains(needle)
                        || hallNames.getOrDefault(issue.getHallId(), "").toLowerCase().contains(needle)
                        || userNames.getOrDefault(issue.getCustomerId(), "").toLowerCase().contains(needle)
                        || issue.getStatus().getLabel().toLowerCase().contains(needle);
                if (!hit) {
                    continue;
                }
            }
            matches.add(issue);
        }
        return matches;
    }

    // ---------- manager actions ----------

    /** Writes the manager's reply to the customer. */
    public void respond(Issue issue, String response) {
        issue.setResponse(Validator.maxLength(
                Validator.text(response, "Response"), "Response", 300));
        issues.update(issue);
    }

    /** Assigns an active scheduler to carry out the fix. */
    public void assign(Issue issue, String schedulerId) {
        if (schedulerId == null || schedulerId.trim().isEmpty()) {
            Validator.fail("Please choose a scheduler to assign.");
        }
        User scheduler = users.findById(schedulerId).orElse(null);
        if (scheduler == null) {
            Validator.fail("That staff member no longer exists.");
        }
        if (scheduler.getRole() != Role.SCHEDULER) {
            Validator.fail("Issues may only be assigned to scheduler staff.");
        }
        if (scheduler.getStatus() != UserStatus.ACTIVE) {
            Validator.fail(scheduler.getDisplayName()
                    + " is blocked and cannot be assigned work.");
        }
        issue.setAssignedSchedulerId(schedulerId);
        issues.update(issue);
    }

    /**
     * Moves an issue between the four states.
     *
     * Marking an issue Done requires an assigned scheduler, since someone must
     * have carried out the work.
     */
    public void changeStatus(Issue issue, IssueStatus status) {
        if (status == null) {
            Validator.fail("Please choose a status.");
        }
        if (status == issue.getStatus()) {
            Validator.fail("This issue is already marked as " + status.getLabel() + ".");
        }
        if (status == IssueStatus.DONE && !issue.isAssigned()) {
            Validator.fail("Assign a scheduler before marking the issue as Done.");
        }
        issue.setStatus(status);
        issues.update(issue);
    }

    /** Active schedulers, for the assignment dropdown. */
    public List<User> assignableSchedulers() {
        List<User> available = new ArrayList<>();
        for (User user : users.findByRole(Role.SCHEDULER)) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                available.add(user);
            }
        }
        return available;
    }

    // ---------- lookups for display ----------

    public Map<String, String> hallNamesById() {
        Map<String, String> names = new HashMap<>();
        for (Hall hall : halls.findAll()) {
            names.put(hall.getId(), hall.getName());
        }
        return names;
    }

    public Map<String, String> userNamesById() {
        Map<String, String> names = new HashMap<>();
        for (User user : users.findAll()) {
            names.put(user.getId(), user.getDisplayName());
        }
        return names;
    }
}
