package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemServiceImpl implements ItemService {
    private final Map<Integer, Item> items = new HashMap<>();
    private int nextId = 1;
    private UserServiceImpl userService;

    public ItemServiceImpl(UserServiceImpl userService) {
        this.userService = userService;
    }

    private int getNextId() {
        return nextId++;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Integer ownerId) {
        Item item = ItemMapper.toItem(itemDto, ownerId);
        checkOwner(item);
        validate(item);
        item.setId(getNextId());
        item.setOwnerId(ownerId);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(Integer itemId, ItemDto itemDto, Integer ownerId) {
        Item item = items.get(itemId);
        checkId(item);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Item with id = " + itemId + " does not belong to user with id = " + ownerId);
        }
        ItemMapper.updateItem(item, itemDto);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Integer ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Integer itemId, Integer ownerId) {
        checkId(items.get(itemId));
        Item item = items.get(itemId);
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer ownerId) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getOwnerId().equals(ownerId) &&
                                (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                        item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toDto)
                .toList();
    }

    private void checkOwner(Item item) {
        if (item.getOwnerId() == null || item.getOwnerId() < 1) {
            throw new ValidationException("OwnerId should not be not empty and positive");
        }
        if (userService.getUserById(item.getOwnerId()) == null) {
            throw new NotFoundException("User with id = " + item.getOwnerId() + " was not found");
        }
    }

    private void checkId(Item item) {
        if (item.getId() == null || item.getId() < 1) {
            throw new ValidationException("Id should be not empty and positive");
        }
        if (!items.containsKey(item.getId())) {
            throw new ValidationException("Item with id = " + item.getId() + " was not found");
        }
    }

    private void validate(Item item) {
        if (item.getAvailable() == null) {
            throw new ValidationException("Available field should be not empty and positive");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Name should not be empty and must contain @");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Description should not be empty and must contain @");
        }
    }
}
