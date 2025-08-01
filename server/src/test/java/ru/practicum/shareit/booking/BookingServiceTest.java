package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingServiceImpl bookingService;

    private final User naruto = new User(7, "Naruto Uzumaki", "hokage@konoha.jp");
    private final User sasuke = new User(8, "Sasuke Uchiha", "avenger@konoha.jp");

    private final Item rasenganScroll = new Item(3, "Rasengan Scroll", "Forbidden jutsu",
            naruto.getId(), true, null);

    @BeforeEach
    void init() {
        bookingRepository = mock(BookingRepository.class);
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);

        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);
    }

    @Test
    void createBookingTest() {
        BookingDto dto = new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                rasenganScroll.getId(), ItemMapper.toDto(rasenganScroll), null, null);
        when(userRepository.findById(sasuke.getId())).thenReturn(Optional.of(sasuke));
        when(itemRepository.findById(rasenganScroll.getId())).thenReturn(Optional.of(rasenganScroll));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingDto result = bookingService.createBooking(dto, sasuke.getId());

        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any());
    }

    @Test
    @DisplayName("Sasuke can't book his own Chidori scroll")
    void bookingOwnItemTest() {
        Item chidori = new Item(4, "Chidori", "Lightning strike",
                sasuke.getId(), true, null);
        BookingDto dto = new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                chidori.getId(),ItemMapper.toDto(chidori), null, null);
        when(userRepository.findById(sasuke.getId())).thenReturn(Optional.of(sasuke));
        when(itemRepository.findById(chidori.getId())).thenReturn(Optional.of(chidori));

        assertThatThrownBy(() -> bookingService.createBooking(dto, sasuke.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void itemUnavailableTest() {
        Item bell = new Item(10, "Training Bell", "Sensei’s test item",
                naruto.getId(), false, null);
        BookingDto dto = new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                bell.getId(),ItemMapper.toDto(bell), null, null);
        when(userRepository.findById(sasuke.getId())).thenReturn(Optional.of(sasuke));
        when(itemRepository.findById(bell.getId())).thenReturn(Optional.of(bell));

        assertThatThrownBy(() -> bookingService.createBooking(dto, sasuke.getId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void statusAlreadyChangedTest() {
        Item mockedItem = mock(Item.class);
        when(mockedItem.getOwnerId()).thenReturn(naruto.getId());

        Booking booking = mock(Booking.class);
        when(booking.getItem()).thenReturn(mockedItem);
        when(booking.getStatus()).thenReturn(BookingStatus.APPROVED);
        when(bookingRepository.findById(42)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(42, naruto.getId(), false))
                .isInstanceOf(ValidationException.class);

    }

    @Test
    @DisplayName("Neji cannot view someone else's destiny booking")
    void shouldThrowOnForbiddenBookingAccess() {
        Booking destiny = mock(Booking.class);
        when(destiny.getItem()).thenReturn(rasenganScroll);
        when(destiny.getBooker()).thenReturn(new User(99, "Hinata Hyuga", "hinata@konoha.jp"));
        when(bookingRepository.findById(1)).thenReturn(Optional.of(destiny));
        when(userRepository.findById(11)).thenReturn(Optional.of(new User(11,
                "Neji Hyuga", "neji@konoha.jp")));

        assertThatThrownBy(() -> bookingService.getBookingById(1, 11))
                .isInstanceOf(ForbiddenException.class);
    }

    @Nested
    class BookingStates {

        @Test
        void getUserBookingsALLTest() {
            User itachi = new User(11, "Itachi", "genjutsu@akatsuki.com");
            Item tsukuyomiScroll = new Item(42, "Tsukuyomi", "Eternal illusion",
                    99, true, null);
            Booking booking = new Booking(123, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                    tsukuyomiScroll, itachi, BookingStatus.APPROVED);
            when(userRepository.findById(11))
                    .thenReturn(Optional.of(itachi));
            when(bookingRepository.findByBookerId(eq(11), any()))
                    .thenReturn(List.of(booking));

            var result = bookingService.getUserBookings(11, "ALL");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getItem().getName()).isEqualTo("Tsukuyomi");
        }

        @Test
        void getUserBookingsCURRENTTest() {
            User jiraiya = new User(12, "Jiraiya", "pervy@toad.jp");
            Item toadOilScroll = new Item(77, "Toad Oil Scroll", "Ancient toad technique",
                    99, true, null);
            Booking booking = new Booking(1001, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                    toadOilScroll, jiraiya, BookingStatus.APPROVED);

            when(userRepository.findById(12)).thenReturn(Optional.of(new User(12,
                    "Jiraiya", "pervy@toad.jp")));
            when(bookingRepository.findCurrentBookingsByUser(eq(12), any()))
                    .thenReturn(List.of(booking));

            var result = bookingService.getUserBookings(12, "CURRENT");

            assertThat(result).hasSize(1);
        }

        @Test
        void getUserBookingsWAITINGTest() {
            User kiba = new User(13, "Kiba", "inu@konoha.jp");
            Item akamaru = new Item(99, "Akamaru", "Faithful ninja dog",
                    13, true, null);
            Booking booking = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                    akamaru, kiba, BookingStatus.WAITING);

            when(userRepository.findById(13)).thenReturn(Optional.of(new User(13,
                    "Kiba", "inu@konoha.jp")));
            when(bookingRepository.findWaitingBookingsByOwner(13)).thenReturn(List.of(booking));

            var result = bookingService.getOwnerBookings(13, "WAITING");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        }
    }

    @Test
    void getUserBookingsDRUNKENTest() {
        when(userRepository.findById(19)).thenReturn(Optional.of(new User(19,
                "Rock Lee", "bushido@leaf.jp")));

        assertThatThrownBy(() -> bookingService.getUserBookings(19, "DRUNKEN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getOwnerBookingsTest() {
        User gaara = new User(21, "Gaara", "sand@sunavillage.org");
        Item sandGourd = new Item(777, "Sand Gourd", "A weapon made of sand",
                21, true, null);
        Booking desertPrison = new Booking(101, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                sandGourd, gaara, BookingStatus.APPROVED);

        when(userRepository.findById(21)).thenReturn(Optional.of(new User(21,
                "Gaara", "sand@sunavillage.org")));
        when(bookingRepository.findByItemOwnerId(eq(21), any())).thenReturn(List.of(desertPrison, desertPrison));

        var bookings = bookingService.getOwnerBookings(21, "ALL");

        assertThat(bookings).hasSize(2);
    }
}
