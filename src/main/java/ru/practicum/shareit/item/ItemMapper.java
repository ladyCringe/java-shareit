package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemMapper {
    public static Item toItem(ItemDto itemDto, Integer ownerId) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                ownerId,
                itemDto.getAvailable()
        );
    }

    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public static ItemWithBookingsDto toWithBookingsDto(Item item, BookingShortDto lastBooking,
                                                        BookingShortDto nextBooking, List<Comment> comments) {
        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments
        );
    }
}
