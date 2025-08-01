package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto, Integer userId);

    BookingDto approveBooking(Integer bookingId, Integer ownerId, boolean approved);

    BookingDto getBookingById(Integer bookingId, Integer userId);

    List<BookingDto> getUserBookings(Integer userId, String state);

    List<BookingDto> getOwnerBookings(Integer ownerId, String state);
}
