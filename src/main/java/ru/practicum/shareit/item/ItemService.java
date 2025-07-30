package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(@Valid ItemDto itemDto, Integer ownerId);

    ItemDto updateItem(Integer itemId, ItemDto itemDto, Integer ownerId);

    List<ItemWithBookingsDto> getItemsByOwner(Integer ownerId);

    ItemWithBookingsDto getItemById(Integer itemId, Integer ownerId);

    List<ItemDto> searchItems(String text, Integer ownerId);

    CommentDto addComment(Integer itemId, Integer userId, String text);

}
