package repository;

import models.Booking;
import models.enums.BookingStatus;
import utils.DateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Store for bookings. */
public class BookingRepository extends Repository<Booking> {

    public static final String FILE = "data/bookings.txt";

    public BookingRepository() {
        super(FILE,
                "# bookings.txt",
                "# Format: id|customerId|hallId|start|end|totalCost|status|createdAt",
                "# start, end, createdAt use ISO-8601, e.g. 2026-09-01T08:00",
                "# status: PAID | CANCELLED");
    }

    @Override
    protected Booking fromFields(String[] fields) {
        LocalDateTime start = DateUtil.fromStorage(at(fields, 3));
        LocalDateTime end = DateUtil.fromStorage(at(fields, 4));
        double cost = Double.parseDouble(at(fields, 5, "0"));
        return new Booking(
                at(fields, 0),
                at(fields, 1),
                at(fields, 2),
                start,
                end,
                cost,
                BookingStatus.fromStorage(at(fields, 6, BookingStatus.PAID.name())),
                DateUtil.fromStorageOrNull(at(fields, 7)));
    }

    public List<Booking> findByCustomer(String customerId) {
        List<Booking> matches = new ArrayList<>();
        for (Booking booking : findAll()) {
            if (booking.getCustomerId().equals(customerId)) {
                matches.add(booking);
            }
        }
        return matches;
    }

    /** Live (non-cancelled) bookings for one hall - the set a new booking must not clash with. */
    public List<Booking> findActiveByHall(String hallId) {
        List<Booking> matches = new ArrayList<>();
        for (Booking booking : findAll()) {
            if (booking.getHallId().equals(hallId) && booking.isActive()) {
                matches.add(booking);
            }
        }
        return matches;
    }
}
