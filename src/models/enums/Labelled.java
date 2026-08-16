package models.enums;

/**
 * Implemented by every enum in this package so they can share one lookup routine
 * and present a human-readable label in tables and combo boxes.
 */
public interface Labelled {

    String getLabel();

    /**
     * Tolerant reverse lookup used when loading stored data.
     *
     * Matches either the constant name or the display label, ignoring case and
     * treating spaces and hyphens as underscores, so values written by earlier
     * versions of the program (for example "Banquet Hall" or "In progress") still
     * load correctly. Unrecognised input yields {@code fallback} rather than an
     * exception, keeping one stray record from breaking a whole screen.
     */
    static <E extends Enum<E> & Labelled> E parse(Class<E> type, String raw, E fallback) {
        if (raw == null) {
            return fallback;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return fallback;
        }
        String normalised = value.toUpperCase().replace(' ', '_').replace('-', '_');
        for (E candidate : type.getEnumConstants()) {
            if (candidate.name().equals(normalised) || candidate.getLabel().equalsIgnoreCase(value)) {
                return candidate;
            }
        }
        return fallback;
    }
}
