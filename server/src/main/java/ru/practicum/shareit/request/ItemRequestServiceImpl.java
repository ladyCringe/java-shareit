package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponseDto createRequest(Integer userId, ItemRequestDto requestDto) {
        User user = UserMapper.toUser(userService.getUserById(userId));

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, user);
        request = requestRepository.save(request);

        return toResponseDto(request, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getRequestsByUserId(Integer userId) {
        userService.getUserById(userId);
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return toResponseDtoList(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Integer userId) {
        userService.getUserById(userId);
        List<ItemRequest> requests = requestRepository.findAllExcludingUser(userId);
        return toResponseDtoList(requests);
    }

    @Override
    public ItemRequestResponseDto getRequestById(Integer requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " not found"));
        List<Item> items = itemRepository.findByRequestId(requestId);
        return toResponseDto(request, items);
    }

    private List<ItemRequestResponseDto> toResponseDtoList(List<ItemRequest> requests) {
        return requests.stream()
                .map(request -> {
                    List<Item> items = itemRepository.findByRequestId(request.getId());
                    return toResponseDto(request, items);
                })
                .collect(Collectors.toList());
    }

    private ItemRequestResponseDto toResponseDto(ItemRequest request, List<Item> items) {
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

