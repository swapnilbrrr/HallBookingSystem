package repository;

import models.TimeSlot;
import utils.DateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared store for the two kinds of scheduled window.
 *
 * Availability and maintenance records have an identical layout, so parsing and
 * the by-hall lookup are written once here; each concrete repository only says
 * which file it uses and which object to build.
 *
 * @param <T> either {@link models.Availability} or {@link models.Maintenance}
 */
public abstract class TimeSlotRepository<T extends TimeSlot> extends Repository<T> {

    protected TimeSlotRepository(String filePath, String... headerLines) {
        super(filePath, headerLines);
    }

    /** Builds the concrete slot type held by this repository. */
    protected abstract T createSlot(String id, String hallId, LocalDateTime start,
                                    LocalDateTime end, String remarks);

    @Override
    protected final T fromFields(String[] fields) {
        return createSlot(
                at(fields, 0),
                at(fields, 1),
                DateUtil.fromStorage(at(fields, 2)),
                DateUtil.fromStorage(at(fields, 3)),
                at(fields, 4));
    }

    public List<T> findByHall(String hallId) {
        List<T> matches = new ArrayList<>();
        for (T slot : findAll()) {
            if (slot.getHallId().equals(hallId)) {
                matches.add(slot);
            }
        }
        return matches;
    }
}
