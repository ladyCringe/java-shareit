package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    BookingService bookingService;

    static UserDto saitama;
    static UserDto genos;
    static ItemDto capeOfSeriousness;
    static ItemDto glovesOfConsecutivePunches;

    @BeforeAll
    static void setup() {
        saitama = new UserDto(null, "Saitama", "caped@baldy.jp");
        genos = new UserDto(null, "Genos", "cyborg@disciple.jp");

        capeOfSeriousness = new ItemDto(null, "Cape of Seriousness",
                "A flowing cape worn during serious fights", true, null);
        glovesOfConsecutivePunches = new ItemDto(null, "Gloves of Consecutive Punches",
                "Boosts rapid attacks", true, null);
    }

    @Test
    void addItemTest() {
        UserDto hero = userService.createUser(saitama);
        ItemDto gear = itemService.addItem(capeOfSeriousness, hero.getId());

        Assertions.assertThat(gear.getName()).isEqualTo(capeOfSeriousness.getName());
        Assertions.assertThat(gear.getAvailable()).isTrue();
    }

    @Test
    void updateItemTest() {
        UserDto hero = userService.createUser(saitama);
        ItemDto original = itemService.addItem(capeOfSeriousness, hero.getId());

        ItemDto updated = itemService.updateItem(original.getId(), glovesOfConsecutivePunches, hero.getId());

        Assertions.assertThat(updated.getName()).isEqualTo(glovesOfConsecutivePunches.getName());
        Assertions.assertThat(updated.getDescription()).contains("rapid attacks");
    }

    @Test
    void updateItemIfNotOwner() {
        UserDto saitamaUser = userService.createUser(saitama);
        UserDto genosUser = userService.createUser(genos);
        ItemDto gear = itemService.addItem(capeOfSeriousness, saitamaUser.getId());

        Assertions.assertThatThrownBy(() ->
                itemService.updateItem(genosUser.getId(), glovesOfConsecutivePunches, gear.getId())
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void getItemByIdWithNoBookings() {
        UserDto hero = userService.createUser(saitama);
        ItemDto gear = itemService.addItem(capeOfSeriousness, hero.getId());

        ItemWithBookingsDto fetched = itemService.getItemById(gear.getId(), hero.getId());

        Assertions.assertThat(fetched.getName()).contains("Cape");
        Assertions.assertThat(fetched.getLastBooking()).isNull();
        Assertions.assertThat(fetched.getNextBooking()).isNull();
    }

    @Test
    void getItemsByOwnerTest() {
        UserDto hero = userService.createUser(saitama);
        itemService.addItem(capeOfSeriousness, hero.getId());
        itemService.addItem(glovesOfConsecutivePunches, hero.getId());

        List<ItemWithBookingsDto> gearSet = itemService.getItemsByOwner(hero.getId());
        Assertions.assertThat(gearSet).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void searchItemsTest() {
        UserDto hero = userService.createUser(saitama);
        itemService.addItem(capeOfSeriousness, hero.getId());

        List<ItemDto> searchResults = itemService.searchItems("cape", hero.getId());
        Assertions.assertThat(searchResults).isNotEmpty();
    }

    @Test
    void addCommentTest() {
        UserDto saitamaDto = userService.createUser(saitama);
        UserDto genosDto = userService.createUser(genos);

        ItemDto gear = itemService.addItem(capeOfSeriousness, saitamaDto.getId());

        BookingDto mission = new BookingDto(
                gear.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                gear.getId(),
                gear,
                genosDto,
                null
        );
        BookingDto booking = bookingService.createBooking(mission, genosDto.getId());
        bookingService.approveBooking(booking.getId(), saitamaDto.getId(), true);

        itemService.addComment(gear.getId(), genosDto.getId(), "This cape gave me strength.");

        ItemWithBookingsDto reviewedGear = itemService.getItemById(gear.getId(), genosDto.getId());
        Assertions.assertThat(reviewedGear.getComments()).hasSize(1);
    }

    @Test
    void notAddCommentTest() {
        UserDto saitamaDto = userService.createUser(saitama);
        UserDto genosDto = userService.createUser(genos);
        UserDto king = userService.createUser(new UserDto(null, "King", "s-rank@hero.jp"));

        ItemDto gear = itemService.addItem(capeOfSeriousness, saitamaDto.getId());

        BookingDto mission = new BookingDto(
                gear.getId(),
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                gear.getId(),
                gear,
                genosDto,
                null
        );
        BookingDto booking = bookingService.createBooking(mission, genosDto.getId());
        bookingService.approveBooking(saitamaDto.getId(), booking.getId(), true);

        itemService.addComment(gear.getId(), genosDto.getId(), "Too OP!");

        Assertions.assertThatThrownBy(() ->
                itemService.addComment(gear.getId(), king.getId(), "Too OP!")
        ).isInstanceOf(ValidationException.class);
    }

}