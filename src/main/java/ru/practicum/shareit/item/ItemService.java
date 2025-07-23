package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Integer ownerId);

    ItemDto updateItem(Integer itemId, ItemDto itemDto, Integer ownerId);

    List<ItemDto> getItemsByOwner(Integer ownerId);

    ItemDto getItemById(Integer itemId, Integer ownerId);

    List<ItemDto> searchItems(String text, Integer ownerId);
}
