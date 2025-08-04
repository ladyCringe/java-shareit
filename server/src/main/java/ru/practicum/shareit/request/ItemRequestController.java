package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestResponseDto createRequest(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                @RequestBody @Valid ItemRequestDto dto) {
        log.info("New request to create item request: {} from user with id {}", dto, userId);
        ItemRequestResponseDto createdRequest = service.createRequest(userId, dto);
        log.info("Item request successfully created: {}", createdRequest);
        return createdRequest;
    }

    @GetMapping
    public List<ItemRequestResponseDto> getRequestsByUserid(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("New request to get own item requests for user with id {}", userId);
        List<ItemRequestResponseDto> requests = service.getRequestsByUserId(userId);
        log.info("Returned {} item requests for user {}", requests.size(), userId);
        return requests;
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("New request to get all item requests except user {}", userId);
        List<ItemRequestResponseDto> requests = service.getAllRequests(userId);
        log.info("Returned {} item requests (excluding user {})", requests.size(), userId);
        return requests;
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@PathVariable Integer requestId) {
        log.info("New request to get item request with id {}", requestId);
        ItemRequestResponseDto request = service.getRequestById(requestId);
        log.info("Item request with id {} successfully retrieved", requestId);
        return request;
    }
}
