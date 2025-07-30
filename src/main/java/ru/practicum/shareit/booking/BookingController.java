package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingDto bookingDto,
                             @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("New booking request: {}", bookingDto);
        BookingDto createdBooking = bookingService.createBooking(bookingDto, userId);
        log.info("New booking created: {}", createdBooking);
        return createdBooking;
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable Integer bookingId,
                              @RequestParam boolean approved,
                              @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New booking approval request: {}", approved);
        BookingDto approvedBooking = bookingService.approveBooking(bookingId, ownerId, approved);
        log.info("Booking approved: {}", approvedBooking);
        return approvedBooking;
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@PathVariable Integer bookingId,
                          @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("New request to get booking info by id {}", bookingId);
        BookingDto booking = bookingService.getBookingById(bookingId, userId);
        log.info("Booking info successfully displayed");
        return booking;
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                            @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("New request to get user bookings");
        List<BookingDto> bookings = bookingService.getUserBookings(userId, state);
        log.info("User bookings successfully displayed");
        return bookings;
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestParam(defaultValue = "ALL") String state,
                                             @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to get owner bookings");
        List<BookingDto> bookings = bookingService.getOwnerBookings(ownerId, state);
        log.info("Owner bookings successfully displayed");
        return bookings;
    }
}

