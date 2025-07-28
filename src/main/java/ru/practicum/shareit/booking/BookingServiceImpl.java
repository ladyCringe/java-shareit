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
        User booker = getUser(userId);
        if (bookingDto.getItemId() == null) {
            throw new NotFoundException("Item and item id should not be empty");
        }
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }
        if (item.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Owner cannot book their own item");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

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

        return filterBookings(bookingRepository.findByBookerId(userId, Sort.by("start").descending()),
                bookingState, now);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Integer ownerId, String state) {
        getUser(ownerId);
        BookingState bookingState = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        return filterBookings(bookingRepository.findByItemOwnerId(ownerId, Sort.by("start").descending()),
                bookingState, now);
    }

    private List<BookingDto> filterBookings(List<Booking> bookings, BookingState state, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> switch (state) {
                    case ALL -> true;
                    case CURRENT -> b.getStart().isBefore(now) && b.getEnd().isAfter(now);
                    case PAST -> b.getEnd().isBefore(now);
                    case FUTURE -> b.getStart().isAfter(now);
                    case WAITING -> b.getStatus() == BookingStatus.WAITING;
                    case REJECTED -> b.getStatus() == BookingStatus.REJECTED;
                })
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Booking getBooking(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
    }
}
