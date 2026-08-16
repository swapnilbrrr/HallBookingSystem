package utils;

import repository.Persistable;

/**
 * Reusable input checks.
 *
 * Every failure is reported by throwing {@link ValidationException}, which the
 * user interface catches once per form and shows in a dialog. That keeps the
 * validation rules here instead of scattered through the Swing listeners.
 */
public final class Validator {

    /** Thrown when user input cannot be accepted; the message is shown verbatim. */
    public static class ValidationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ValidationException(String message) {
            super(message);
        }
    }

    private Validator() {
    }

    public static void fail(String message) {
        throw new ValidationException(message);
    }

    /** Trimmed, non-empty, and free of the field delimiter. */
    public static String text(String value, String fieldName) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            fail(fieldName + " is required.");
        }
        if (text.contains(Persistable.DELIMITER)) {
            fail(fieldName + " may not contain the '" + Persistable.DELIMITER
                    + "' character, because it separates fields in the data files.");
        }
        return text;
    }

    /** Same as {@link #text} but allows an empty value (returns ""). */
    public static String optionalText(String value, String fieldName) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return "";
        }
        return text(text, fieldName);
    }

    public static String maxLength(String value, String fieldName, int max) {
        if (value != null && value.length() > max) {
            fail(fieldName + " may be at most " + max + " characters.");
        }
        return value;
    }

    public static int positiveInt(String value, String fieldName) {
        int parsed = parseInt(value, fieldName);
        if (parsed <= 0) {
            fail(fieldName + " must be greater than zero.");
        }
        return parsed;
    }

    public static int intInRange(String value, String fieldName, int min, int max) {
        int parsed = parseInt(value, fieldName);
        if (parsed < min || parsed > max) {
            fail(fieldName + " must be between " + min + " and " + max + ".");
        }
        return parsed;
    }

    public static double positiveDouble(String value, String fieldName) {
        String text = text(value, fieldName);
        double parsed;
        try {
            parsed = Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            fail(fieldName + " must be a number.");
            return 0d; // unreachable
        }
        if (parsed <= 0) {
            fail(fieldName + " must be greater than zero.");
        }
        return parsed;
    }

    public static String username(String value) {
        String text = text(value, "Username");
        if (text.length() < 3) {
            fail("Username must be at least 3 characters.");
        }
        if (!text.matches("[A-Za-z0-9._-]+")) {
            fail("Username may only contain letters, digits, dots, underscores and hyphens.");
        }
        return text;
    }

    public static String password(String value) {
        String text = value == null ? "" : value;
        if (text.trim().isEmpty()) {
            fail("Password is required.");
        }
        if (text.length() < 5) {
            fail("Password must be at least 5 characters.");
        }
        if (text.contains(Persistable.DELIMITER)) {
            fail("Password may not contain the '" + Persistable.DELIMITER + "' character.");
        }
        return text;
    }

    /** Optional field: blank passes, otherwise it must look like an address. */
    public static String email(String value) {
        String text = optionalText(value, "Email");
        if (!text.isEmpty() && !text.matches("[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}")) {
            fail("Email must be a valid address, for example name@example.com.");
        }
        return text;
    }

    /** Optional field: blank passes, otherwise 7-15 digits with optional +/spaces. */
    public static String phone(String value) {
        String text = optionalText(value, "Phone");
        if (!text.isEmpty() && !text.matches("\\+?[0-9][0-9 \\-]{6,14}")) {
            fail("Phone must be 7 to 15 digits, optionally starting with '+'.");
        }
        return text;
    }

    private static int parseInt(String value, String fieldName) {
        String text = text(value, fieldName);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            fail(fieldName + " must be a whole number.");
            return 0; // unreachable
        }
    }
}
