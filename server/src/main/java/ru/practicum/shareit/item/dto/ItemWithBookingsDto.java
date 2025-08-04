package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comments.model.Comment;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemWithBookingsDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<Comment> comments;
}
