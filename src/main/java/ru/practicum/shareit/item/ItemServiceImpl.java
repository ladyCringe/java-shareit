package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {
    private final Map<Integer, Item> items = new HashMap<>();
    private int nextId = 1;
    private final UserServiceImpl userService;

    public ItemServiceImpl(UserServiceImpl userService) {
        this.userService = userService;
    }

    private int getNextId() {
        return nextId++;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Integer ownerId) {
        checkOwner(ownerId);
        Item item = ItemMapper.toItem(itemDto, ownerId);
        item.setId(getNextId());
        item.setOwnerId(ownerId);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(Integer itemId, ItemDto itemDto, Integer ownerId) {
        Item item = checkId(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Item with id = " + itemId + " does not belong to user with id = " + ownerId);
        }
        update(item, itemDto);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Integer ownerId) {
        checkOwner(ownerId);
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Integer itemId, Integer ownerId) {
        checkOwner(ownerId);
        Item item = checkId(itemId);
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer ownerId) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        checkOwner(ownerId);
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getOwnerId().equals(ownerId) &&
                                (item.getName().toLowerCase().contains(lowerText) ||
                                        item.getDescription().toLowerCase().contains(lowerText)))
                .map(ItemMapper::toDto)
                .toList();
    }

    private void checkOwner(Integer ownerId) {
        if (ownerId == null || ownerId < 1) {
            throw new ValidationException("OwnerId should not be not empty and positive");
        }
        userService.getUserById(ownerId);
    }

    private Item checkId(Integer itemId) {
        if (itemId == null || itemId < 1) {
            throw new ValidationException("Id should be not empty and positive");
        }
        if (!items.containsKey(itemId)) {
            throw new ValidationException("Item with id = " + itemId + " was not found");
        }
        return items.get(itemId);
    }

    private void update(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }
}
