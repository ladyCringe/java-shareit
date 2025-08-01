package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comments.CommentMapper;
import ru.practicum.shareit.comments.CommentRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserServiceImpl userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;


    public ItemServiceImpl(ItemRepository itemRepository, UserServiceImpl userService,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Integer ownerId) {
        userService.getUserById(ownerId);

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request with id = " +
                            itemDto.getRequestId() + " not found"));
        }

        Item savedItem = itemRepository.save(ItemMapper.toItem(itemDto, ownerId, request));
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Integer itemId, ItemDto itemDto, Integer ownerId) {
        Item item = checkId(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Item with id = " + itemId + " does not belong to user with id = " + ownerId);
        }
        update(item, itemDto);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public List<ItemWithBookingsDto> getItemsByOwner(Integer ownerId) {
        userService.getUserById(ownerId);
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Integer> itemIds = items.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = bookingRepository.findLastBookingsForItems(itemIds, now);
        List<Booking> nextBookings = bookingRepository.findNextBookingsForItems(itemIds, now);

        Map<Integer, Booking> lastMap = lastBookings.stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));

        Map<Integer, Booking> nextMap = nextBookings.stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));

        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);
        Map<Integer, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> {
                    BookingShortDto last = Optional.ofNullable(lastMap.get(item.getId()))
                            .map(BookingMapper::toShortDto).orElse(null);

                    BookingShortDto next = Optional.ofNullable(nextMap.get(item.getId()))
                            .map(BookingMapper::toShortDto).orElse(null);

                    return new ItemWithBookingsDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getAvailable(),
                            last,
                            next,
                            commentsByItem.getOrDefault(item.getId(), List.of())
                    );
                })
                .toList();
    }

    @Override
    public ItemWithBookingsDto getItemById(Integer itemId, Integer ownerId) {
        userService.getUserById(ownerId);
        Item item = checkId(itemId);
        BookingShortDto last = null;
        BookingShortDto next = null;
        if (item.getOwnerId().equals(ownerId)) {
            last = BookingMapper.toShortDto(bookingRepository.findLastBooking(itemId, APPROVED,
                    LocalDateTime.now()));
            next = BookingMapper.toShortDto(bookingRepository.findNextBooking(itemId, LocalDateTime.now()));
        }
        return ItemMapper.toWithBookingsDto(item,
                last,
                next,
                commentRepository.findByItemId(itemId));
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer ownerId) {
        userService.getUserById(ownerId);
        String lowerText = text.toLowerCase();
        return itemRepository.searchAvailableItems(lowerText).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Integer itemId, Integer userId, String text) {
        Item item = checkId(itemId);
        UserDto user = userService.getUserById(userId);

        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), APPROVED
        );
        if (!hasBooked) {
            throw new ValidationException("User has not completed a booking for this item");
        }

        Comment comment = CommentMapper.toComment(text, item, UserMapper.toUser(user));
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private Item checkId(Integer itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item with id = " + itemId + " was not found"));
    }

    private void update(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }
}
