package repository;

import models.Administrator;
import models.Customer;
import models.Manager;
import models.Scheduler;
import models.User;
import models.enums.Role;
import models.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Store for every account.
 *
 * {@link #instantiate} is a small factory: the role recorded in the file decides
 * which {@link User} subclass is built, which is what lets the login screen call
 * {@code user.showDashboard()} without ever testing the role itself.
 */
public class UserRepository extends Repository<User> {

    public static final String FILE = "data/users.txt";

    public UserRepository() {
        super(FILE,
                "# users.txt",
                "# Format: id|username|password|role|status|fullName|email|phone",
                "# role:   CUSTOMER | SCHEDULER | ADMINISTRATOR | MANAGER",
                "# status: ACTIVE | BLOCKED");
    }

    @Override
    protected User fromFields(String[] fields) {
        return instantiate(
                Role.fromStorage(at(fields, 3)),
                at(fields, 0),
                at(fields, 1),
                at(fields, 2),
                UserStatus.fromStorage(at(fields, 4, UserStatus.ACTIVE.name())),
                at(fields, 5),
                at(fields, 6),
                at(fields, 7));
    }

    /** Builds the concrete subclass that matches {@code role}. */
    public static User instantiate(Role role, String id, String username, String password,
                                   UserStatus status, String fullName, String email,
                                   String phone) {
        switch (role) {
            case SCHEDULER:
                return new Scheduler(id, username, password, status, fullName, email, phone);
            case ADMINISTRATOR:
                return new Administrator(id, username, password, status, fullName, email, phone);
            case MANAGER:
                return new Manager(id, username, password, status, fullName, email, phone);
            case CUSTOMER:
            default:
                return new Customer(id, username, password, status, fullName, email, phone);
        }
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        String needle = username.trim();
        for (User user : findAll()) {
            if (user.getUsername().equalsIgnoreCase(needle)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    public List<User> findByRole(Role role) {
        List<User> matches = new ArrayList<>();
        for (User user : findAll()) {
            if (user.getRole() == role) {
                matches.add(user);
            }
        }
        return matches;
    }
}
