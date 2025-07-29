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

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByUser(Integer userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByOwner(Integer ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastBookingsByUser(Integer userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastBookingsByOwner(Integer ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByUser(Integer userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByOwner(Integer ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findWaitingBookingsByUser(Integer userId);

    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findWaitingBookingsByOwner(Integer ownerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findRejectedBookingsByUser(Integer userId);

    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findRejectedBookingsByOwner(Integer ownerId);

    @Query("""
    SELECT b FROM Booking b
    WHERE b.item.id IN :itemIds
      AND b.status = 'APPROVED'
      AND b.start < :now
      AND b.start = (
          SELECT MAX(b2.start) FROM Booking b2
          WHERE b2.item.id = b.item.id
            AND b2.status = 'APPROVED'
            AND b2.start < :now
      )
""")
    List<Booking> findLastBookingsForItems(List<Integer> itemIds, LocalDateTime now);

    @Query("""
    SELECT b FROM Booking b
    WHERE b.item.id IN :itemIds
      AND b.status = 'APPROVED'
      AND b.start > :now
      AND b.start = (
          SELECT MIN(b2.start) FROM Booking b2
          WHERE b2.item.id = b.item.id
            AND b2.status = 'APPROVED'
            AND b2.start > :now
      )
""")
    List<Booking> findNextBookingsForItems(List<Integer> itemIds, LocalDateTime now);

}