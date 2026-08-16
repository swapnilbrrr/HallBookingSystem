package services;

import models.Booking;
import models.User;
import models.enums.Role;
import models.enums.UserStatus;
import repository.BookingRepository;
import repository.UserRepository;
import utils.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Account administration: the operations behind the Administrator's
 * "Scheduler Staff Management" and "User Management" screens.
 *
 * Kept apart from {@link AuthService} so that signing in stays separate from
 * managing other people's accounts.
 */
public class UserService {

    private final UserRepository users = new UserRepository();
    private final BookingRepository bookings = new BookingRepository();

    // ---------- queries ----------

    public List<User> findAll() {
        return users.findAll();
    }

    public List<User> findByRole(Role role) {
        return users.findByRole(role);
    }

    public List<User> findSchedulers() {
        return users.findByRole(Role.SCHEDULER);
    }

    public Optional<User> findById(String id) {
        return users.findById(id);
    }

    /**
     * Filters accounts by free text, role and status. A null role or status means
     * "any", and blank text matches everything, so one method serves every
     * combination the screens offer.
     */
    public List<User> filter(String query, Role role, UserStatus status) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        List<User> matches = new ArrayList<>();
        for (User user : users.findAll()) {
            if (role != null && user.getRole() != role) {
                continue;
            }
            if (status != null && user.getStatus() != status) {
                continue;
            }
            if (!needle.isEmpty() && !matchesText(user, needle)) {
                continue;
            }
            matches.add(user);
        }
        return matches;
    }

    private static boolean matchesText(User user, String needle) {
        return user.getId().toLowerCase().contains(needle)
                || user.getUsername().toLowerCase().contains(needle)
                || user.getFullName().toLowerCase().contains(needle)
                || user.getEmail().toLowerCase().contains(needle)
                || user.getPhone().toLowerCase().contains(needle)
                || user.getRoleLabel().toLowerCase().contains(needle);
    }

    // ---------- staff management ----------

    /** Creates a staff account of the given role and returns it. */
    public User createStaff(Role role, String username, String password, String fullName,
                            String email, String phone) {
        if (role == null || !role.isStaff()) {
            Validator.fail("Please choose a staff role.");
        }
        String name = Validator.username(username);
        Validator.password(password);
        if (users.usernameExists(name)) {
            Validator.fail("That username is already taken. Please choose another.");
        }

        User staff = UserRepository.instantiate(role, users.nextId("U"), name, password,
                UserStatus.ACTIVE,
                Validator.optionalText(fullName, "Full name"),
                Validator.email(email),
                Validator.phone(phone));
        users.insert(staff);
        return staff;
    }

    /** Edits an existing account's username and contact details. */
    public void updateAccount(User user, String username, String fullName, String email,
                              String phone) {
        String name = Validator.username(username);
        Optional<User> clash = users.findByUsername(name);
        if (clash.isPresent() && !clash.get().getId().equals(user.getId())) {
            Validator.fail("That username is already taken by " + clash.get().getId() + ".");
        }
        user.setUsername(name);
        user.setFullName(Validator.optionalText(fullName, "Full name"));
        user.setEmail(Validator.email(email));
        user.setPhone(Validator.phone(phone));
        users.update(user);
    }

    /** Sets a new password on behalf of a user who cannot sign in. */
    public void resetPassword(User user, String newPassword) {
        Validator.password(newPassword);
        user.setPassword(newPassword);
        users.update(user);
    }

    // ---------- blocking and deletion ----------

    /**
     * Blocks or unblocks an account.
     *
     * Administrators may not block themselves, and the last active administrator
     * may not be blocked, since that would lock everyone out of the system.
     */
    public void setStatus(User actor, User target, UserStatus status) {
        if (status == UserStatus.BLOCKED) {
            guardSelf(actor, target, "block");
            guardLastAdministrator(target, "blocked");
        }
        target.setStatus(status);
        users.update(target);
    }

    /**
     * Permanently removes an account.
     *
     * Refused when the customer still has upcoming bookings, because the booking
     * records would then point at a user who no longer exists; the administrator
     * is told to block the account instead.
     */
    public void delete(User actor, User target) {
        guardSelf(actor, target, "delete");
        guardLastAdministrator(target, "deleted");

        List<Booking> upcoming = new ArrayList<>();
        for (Booking booking : bookings.findByCustomer(target.getId())) {
            if (booking.isUpcoming()) {
                upcoming.add(booking);
            }
        }
        if (!upcoming.isEmpty()) {
            Validator.fail(target.getUsername() + " still has " + upcoming.size()
                    + " upcoming booking(s), so the account cannot be deleted."
                    + " Block the account instead, or wait until the bookings have passed.");
        }
        users.delete(target.getId());
    }

    private static void guardSelf(User actor, User target, String action) {
        if (actor != null && actor.getId().equals(target.getId())) {
            Validator.fail("You cannot " + action + " your own account.");
        }
    }

    private void guardLastAdministrator(User target, String pastTenseAction) {
        if (target.getRole() != Role.ADMINISTRATOR) {
            return;
        }
        int activeAdmins = 0;
        for (User user : users.findByRole(Role.ADMINISTRATOR)) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                activeAdmins++;
            }
        }
        if (activeAdmins <= 1) {
            Validator.fail("This is the only active administrator, so the account cannot be "
                    + pastTenseAction + ".");
        }
    }
}
