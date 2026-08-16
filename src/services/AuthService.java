package services;

import models.Customer;
import models.User;
import repository.UserRepository;
import utils.Validator;

import java.util.Optional;

/**
 * Authentication and self-service account changes.
 *
 * Every method is an instance method, and every rejection is reported by
 * throwing {@link Validator.ValidationException} carrying a message fit to show
 * the user. A screen therefore needs one catch block rather than a chain of
 * null checks.
 */
public class AuthService {

    private final UserRepository users = new UserRepository();

    /**
     * Verifies credentials and returns the matching account.
     *
     * @throws Validator.ValidationException if the credentials are wrong or the
     *         account has been blocked by an administrator
     */
    public User login(String username, String password) {
        String name = username == null ? "" : username.trim();
        String secret = password == null ? "" : password;

        if (name.isEmpty() || secret.isEmpty()) {
            Validator.fail("Please enter both your username and password.");
        }

        Optional<User> found = users.findByUsername(name);
        if (!found.isPresent() || !found.get().getPassword().equals(secret)) {
            // Deliberately vague: do not reveal whether the username exists.
            Validator.fail("Invalid username or password.");
        }

        User user = found.get();
        if (!user.canLogin()) {
            Validator.fail("This account has been blocked. Please contact the administrator.");
        }
        return user;
    }

    /** Registers a new customer account and returns it. */
    public Customer registerCustomer(String username, String password, String confirmPassword,
                                     String fullName, String email, String phone) {
        String name = Validator.username(username);
        Validator.password(password);
        if (!password.equals(confirmPassword)) {
            Validator.fail("The two passwords do not match.");
        }
        if (users.usernameExists(name)) {
            Validator.fail("That username is already taken. Please choose another.");
        }

        Customer customer = new Customer(
                users.nextId("U"),
                name,
                password,
                models.enums.UserStatus.ACTIVE,
                Validator.optionalText(fullName, "Full name"),
                Validator.email(email),
                Validator.phone(phone));
        users.insert(customer);
        return customer;
    }

    /** Changes a password after confirming the current one. */
    public void changePassword(User user, String currentPassword, String newPassword,
                               String confirmPassword) {
        if (!user.getPassword().equals(currentPassword == null ? "" : currentPassword)) {
            Validator.fail("Your current password is incorrect.");
        }
        Validator.password(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            Validator.fail("The two new passwords do not match.");
        }
        if (newPassword.equals(currentPassword)) {
            Validator.fail("The new password must be different from the current one.");
        }
        user.setPassword(newPassword);
        users.update(user);
    }

    /** Updates the editable parts of a profile; the username and role are fixed. */
    public void updateProfile(User user, String fullName, String email, String phone) {
        user.setFullName(Validator.optionalText(fullName, "Full name"));
        user.setEmail(Validator.email(email));
        user.setPhone(Validator.phone(phone));
        users.update(user);
    }

    /** Re-reads an account from storage, so a screen can refresh after a change. */
    public Optional<User> reload(String userId) {
        return users.findById(userId);
    }
}
