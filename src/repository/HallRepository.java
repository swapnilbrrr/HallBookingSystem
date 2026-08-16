package repository;

import models.Hall;
import models.enums.HallStatus;
import models.enums.HallType;

import java.util.ArrayList;
import java.util.List;

/** Store for hall records. */
public class HallRepository extends Repository<Hall> {

    public static final String FILE = "data/halls.txt";

    public HallRepository() {
        super(FILE,
                "# halls.txt",
                "# Format: id|name|type|capacity|ratePerHour|status",
                "# type:   AUDITORIUM | BANQUET_HALL | MEETING_ROOM",
                "# status: ACTIVE | INACTIVE");
    }

    @Override
    protected Hall fromFields(String[] fields) {
        HallType type = HallType.fromStorage(at(fields, 2));
        int capacity = parseIntOr(at(fields, 3), type.getDefaultCapacity());
        double rate = parseDoubleOr(at(fields, 4), type.getRatePerHour());
        return new Hall(
                at(fields, 0),
                at(fields, 1),
                type,
                capacity,
                rate,
                HallStatus.fromStorage(at(fields, 5, HallStatus.ACTIVE.name())));
    }

    /** Halls currently on offer; deleted halls are kept but hidden. */
    public List<Hall> findActive() {
        List<Hall> active = new ArrayList<>();
        for (Hall hall : findAll()) {
            if (hall.isActive()) {
                active.add(hall);
            }
        }
        return active;
    }

    private static int parseIntOr(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static double parseDoubleOr(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
