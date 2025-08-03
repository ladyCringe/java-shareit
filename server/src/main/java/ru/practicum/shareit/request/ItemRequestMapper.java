package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, User user) {
        return new ItemRequest(
                null,
                dto.getDescription(),
                user,
                LocalDateTime.now()
        );
    }

    public static ItemRequestResponseDto toResponseDto(ItemRequest request, List<Item> items) {
        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items.stream()
                        .map(ItemMapper::toDto)
                        .collect(Collectors.toList())
        );
    }
}
