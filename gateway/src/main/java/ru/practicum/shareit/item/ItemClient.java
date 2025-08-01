package ru.practicum.shareit.item;

import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(Integer ownerId, ItemDto itemDto) {
        checkOwner(ownerId);
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Integer itemId, Integer ownerId, ItemDto itemDto) {
        checkId(itemId);
        return patch("/" + itemId, ownerId, itemDto);
    }

    public ResponseEntity<Object> getItemsByOwner(Integer ownerId) {
        checkOwner(ownerId);
        return get("", ownerId);
    }

    public ResponseEntity<Object> getItemById(Integer itemId, Integer ownerId) {
        checkOwner(ownerId);
        checkId(itemId);
        return get("/" + itemId, ownerId);
    }

    public ResponseEntity<Object> searchItems(String text, Integer ownerId) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        checkOwner(ownerId);
        return get("/search?text={text}", ownerId, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(Integer itemId, Integer userId, Map<String, String> body) {
        if (StringUtils.isBlank(body.get("text"))) {
            throw new ValidationException("Comment should not be empty");
        }
        checkId(itemId);
        return post("/" + itemId + "/comment", userId, body);
    }

    private void checkOwner(Integer ownerId) {
        if (ownerId == null || ownerId < 1) {
            throw new ValidationException("OwnerId should not be not empty and positive");
        }
    }

    private void checkId(Integer itemId) {
        if (itemId == null || itemId < 1) {
            throw new ValidationException("Id should be not empty and positive");
        }
    }
}

