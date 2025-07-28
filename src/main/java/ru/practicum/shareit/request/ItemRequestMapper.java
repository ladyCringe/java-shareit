package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return new ItemRequest(
                null,
                dto.getDescription(),
                dto.getRequestor(),
                LocalDateTime.now()
        );
    }

    public static ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequestor(),
                request.getCreated()
        );
    }
}
