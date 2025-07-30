package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Integer userId) {
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ValidationException("Start date should be before end date");
        }
        User booker = getUser(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item with id = " + bookingDto.getItemId() + " not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }
        if (item.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Owner cannot book their own item");
        }

        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approveBooking(Integer bookingId, Integer ownerId, boolean approved) {
        Booking booking = getBooking(bookingId);
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Only the owner can approve or reject a booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already approved or rejected");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Integer bookingId, Integer userId) {
        getUser(userId);
        Booking booking = getBooking(bookingId);
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwnerId().equals(userId)) {
            throw new ForbiddenException("Access denied to booking");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Integer userId, String state) {
        getUser(userId);
        BookingState bookingState = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBookerId(userId, Sort.by("start").descending());
            case CURRENT -> bookingRepository.findCurrentBookingsByUser(userId, now);
            case PAST -> bookingRepository.findPastBookingsByUser(userId, now);
            case FUTURE -> bookingRepository.findFutureBookingsByUser(userId, now);
            case WAITING -> bookingRepository.findWaitingBookingsByUser(userId);
            case REJECTED -> bookingRepository.findRejectedBookingsByUser(userId);
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Integer ownerId, String state) {
        getUser(ownerId);
        BookingState bookingState = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByItemOwnerId(ownerId, Sort.by("start").descending());
            case CURRENT -> bookingRepository.findCurrentBookingsByOwner(ownerId, now);
            case PAST -> bookingRepository.findPastBookingsByOwner(ownerId, now);
            case FUTURE -> bookingRepository.findFutureBookingsByOwner(ownerId, now);
            case WAITING -> bookingRepository.findWaitingBookingsByOwner(ownerId);
            case REJECTED -> bookingRepository.findRejectedBookingsByOwner(ownerId);
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));
    }

    private Booking getBooking(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id = " + bookingId + " not found"));
    }
}
