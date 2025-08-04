package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") Integer ownerId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Creating item {}, ownerId={}", itemDto, ownerId);
        return itemClient.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Integer itemId,
                                             @RequestHeader("X-Sharer-User-Id") Integer ownerId,
                                             @RequestBody ItemDto itemDto) {
        log.info("Updating item with id {}, ownerId={}, data={}", itemId, ownerId, itemDto);
        return itemClient.updateItem(itemId, ownerId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("Getting items for ownerId={}", ownerId);
        return itemClient.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Integer itemId,
                                              @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("Getting item with id={}, ownerId={}", itemId, ownerId);
        return itemClient.getItemById(itemId, ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.info("Searching items with text='{}', ownerId={}", text, ownerId);
        return itemClient.searchItems(text, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Integer itemId,
                                             @RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @RequestBody Map<String, String> body) {
        String commentText = body.get("text");
        log.info("Adding comment to itemId={}, userId={}, text='{}'", itemId, userId, commentText);
        return itemClient.addComment(itemId, userId, body);
    }
}
