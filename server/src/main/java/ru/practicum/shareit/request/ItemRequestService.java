package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto createRequest(Integer userId, ItemRequestDto requestDto);

    List<ItemRequestResponseDto> getRequestsByUserId(Integer userId);

    List<ItemRequestResponseDto> getAllRequests(Integer userId);

    ItemRequestResponseDto getRequestById(Integer requestId);
}
