package repository;

/**
 * Contract for anything stored as one line in a .txt file.
 *
 * The delimiter and the split/join helpers live here so that every model and
 * every repository agrees on the record format.
 */
public interface Persistable {

    /** Field separator for all data files. Chosen over ',' so that free-text
     *  fields such as issue descriptions and schedule remarks may contain commas. */
    String DELIMITER = "|";

    String getId();

    /** Serialises this object into a single delimited line. */
    String toLine();

    /** Joins fields in declaration order, rendering nulls as empty strings. */
    static String join(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(DELIMITER);
            }
            sb.append(fields[i] == null ? "" : String.valueOf(fields[i]));
        }
        return sb.toString();
    }

    /** Splits a stored line, keeping trailing empty fields (limit -1). */
    static String[] split(String line) {
        return line.split("\\" + DELIMITER, -1);
    }
}
