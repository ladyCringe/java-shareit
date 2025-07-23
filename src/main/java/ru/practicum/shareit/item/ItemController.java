package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to add item {} for owner with id {}", itemDto, ownerId);
        ItemDto createdItem = itemService.addItem(itemDto, ownerId);
        log.info("New item {} successfully created", createdItem);
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Integer itemId, @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to update item {} for owner with id {}", itemDto, ownerId);
        ItemDto updatedItem = itemService.updateItem(itemId, itemDto, ownerId);
        log.info("Item {} successfully updated", updatedItem);
        return updatedItem;
    }

    @GetMapping
    public Iterable<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to get items for owner with id {}", ownerId);
        List<ItemDto> items = itemService.getItemsByOwner(ownerId);
        log.info("Items successfully displayed");
        return items;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Integer itemId,
                               @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to get item with id {} for owner with id {}", itemId, ownerId);
        ItemDto item = itemService.getItemById(itemId, ownerId);
        log.info("Item with id {} successfully displayed", itemId);
        return item;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to search items with text {}", text);
        List<ItemDto> items = itemService.searchItems(text, ownerId);
        log.info("Item successfully displayed");
        return items;
    }
}
