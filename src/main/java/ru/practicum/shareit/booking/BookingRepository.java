package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByBookerId(Integer bookerId, Sort sort);

    List<Booking> findByItemOwnerId(Integer ownerId, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = :status " +
            "AND b.start < :now " +
            "ORDER BY b.start DESC")
    Booking findLastBooking(Integer itemId, BookingStatus status, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = ?1 AND b.start > ?2 AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Booking findNextBooking(Integer itemId, LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(Integer userId, Integer itemId,
                                                           LocalDateTime end, BookingStatus status);

}