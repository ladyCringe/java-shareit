package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto,
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
    public Iterable<ItemWithBookingsDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to get items for owner with id {}", ownerId);
        List<ItemWithBookingsDto> items = itemService.getItemsByOwner(ownerId);
        log.info("Items successfully displayed");
        return items;
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@PathVariable Integer itemId,
                               @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("New request to get item with id {} for owner with id {}", itemId, ownerId);
        ItemWithBookingsDto item = itemService.getItemById(itemId, ownerId);
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

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Integer itemId,
                                 @RequestBody Map<String, String> body,
                                 @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("New request to add comment for item with id {} by user with id {}", itemId, userId);
        CommentDto comment = itemService.addComment(itemId, userId, body.get("text"));
        log.info("Comment successfully added");
        return comment;
    }

}
