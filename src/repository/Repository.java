package repository;

import utils.FileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic text-file store shared by every entity type.
 *
 * Subclasses supply only the file location, its comment header, and how to turn
 * a split line back into an object ({@link #fromFields}); reading, writing,
 * updating, deleting and identifier generation are inherited. This is the
 * template-method pattern, and it replaces the per-screen parsing loops that the
 * project previously duplicated in every Swing listener.
 *
 * @param <T> the entity type held in this file
 */
public abstract class Repository<T extends Persistable> {

    private final String filePath;
    private final String[] headerLines;

    protected Repository(String filePath, String... headerLines) {
        this.filePath = filePath;
        this.headerLines = headerLines;
        ensureFileExists();
    }

    /** Rebuilds one entity from its already-split fields. */
    protected abstract T fromFields(String[] fields);

    // ---------- queries ----------

    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        for (String line : FileHandler.readLines(filePath)) {
            if (isSkippable(line)) {
                continue;
            }
            try {
                T entity = fromFields(Persistable.split(line.trim()));
                if (entity != null) {
                    results.add(entity);
                }
            } catch (RuntimeException ex) {
                // One bad record must not stop the whole screen from loading.
                System.err.println("Skipping malformed record in " + filePath + ": " + line
                        + " (" + ex.getMessage() + ")");
            }
        }
        return results;
    }

    public Optional<T> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        for (T entity : findAll()) {
            if (entity.getId().equals(id.trim())) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    public int count() {
        return findAll().size();
    }

    // ---------- mutations ----------

    public void insert(T entity) {
        FileHandler.appendLine(filePath, entity.toLine());
    }

    /** Replaces the record whose identifier matches, preserving comments and order. */
    public boolean update(T entity) {
        List<String> output = new ArrayList<>();
        boolean replaced = false;
        for (String line : FileHandler.readLines(filePath)) {
            if (!isSkippable(line) && idOf(line).equals(entity.getId())) {
                output.add(entity.toLine());
                replaced = true;
            } else {
                output.add(line);
            }
        }
        if (replaced) {
            FileHandler.writeAllLines(filePath, output);
        }
        return replaced;
    }

    /** Removes the record whose identifier matches, preserving comments. */
    public boolean delete(String id) {
        List<String> output = new ArrayList<>();
        boolean removed = false;
        for (String line : FileHandler.readLines(filePath)) {
            if (!isSkippable(line) && idOf(line).equals(id)) {
                removed = true;
                continue;
            }
            output.add(line);
        }
        if (removed) {
            FileHandler.writeAllLines(filePath, output);
        }
        return removed;
    }

    /**
     * Next free identifier of the form {@code prefix + 3 digits}.
     *
     * The highest existing suffix is used rather than the record count, so that
     * deleting a record can never cause a later insert to reuse a live id.
     */
    public String nextId(String prefix) {
        int highest = 0;
        for (String line : FileHandler.readLines(filePath)) {
            if (isSkippable(line)) {
                continue;
            }
            String id = idOf(line);
            if (!id.startsWith(prefix)) {
                continue;
            }
            try {
                highest = Math.max(highest, Integer.parseInt(id.substring(prefix.length())));
            } catch (NumberFormatException ignored) {
                // Legacy identifiers that are not prefix+number simply do not participate.
            }
        }
        return prefix + String.format("%03d", highest + 1);
    }

    // ---------- helpers for subclasses ----------

    /** Field at {@code index}, trimmed, or "" when the record is shorter. */
    protected static String at(String[] fields, int index) {
        return index < fields.length ? fields[index].trim() : "";
    }

    /** Field at {@code index}, falling back to {@code defaultValue} when absent or blank. */
    protected static String at(String[] fields, int index, String defaultValue) {
        String value = at(fields, index);
        return value.isEmpty() ? defaultValue : value;
    }

    protected String getFilePath() {
        return filePath;
    }

    // ---------- internals ----------

    private static boolean isSkippable(String line) {
        String trimmed = line == null ? "" : line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }

    private static String idOf(String line) {
        return Persistable.split(line.trim())[0].trim();
    }

    /** Creates the file with its documenting header the first time it is needed. */
    private void ensureFileExists() {
        File file = new File(filePath);
        if (file.exists()) {
            return;
        }
        List<String> header = new ArrayList<>();
        for (String line : headerLines) {
            header.add(line);
        }
        FileHandler.writeAllLines(filePath, header);
    }
}
