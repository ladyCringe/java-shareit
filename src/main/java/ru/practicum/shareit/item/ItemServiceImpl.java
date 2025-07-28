package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comments.CommentMapper;
import ru.practicum.shareit.comments.CommentRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserServiceImpl userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    public ItemServiceImpl(ItemRepository itemRepository, UserServiceImpl userService,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Integer ownerId) {
        checkOwner(ownerId);
        Item savedItem = itemRepository.save(ItemMapper.toItem(itemDto, ownerId));
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
        checkOwner(ownerId);
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    BookingShortDto last =
                            BookingMapper.toShortDto(bookingRepository.findLastBooking(item.getId(), APPROVED, now));

                    BookingShortDto next =
                            BookingMapper.toShortDto(bookingRepository.findNextBooking(item.getId(), now));

                    return new ItemWithBookingsDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getAvailable(),
                            last,
                            next,
                            commentRepository.findByItemId(item.getId())
                    );
                })
                .toList();
    }

    @Override
    public ItemWithBookingsDto getItemById(Integer itemId, Integer ownerId) {
        checkOwner(ownerId);
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
        if (text == null || text.isBlank()) {
            return List.of();
        }
        checkOwner(ownerId);
        String lowerText = text.toLowerCase();
        return itemRepository.searchAvailableItems(lowerText).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Integer itemId, Integer userId, String text) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));

        UserDto user = userService.getUserById(userId);

        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), APPROVED
        );
        if (!hasBooked) {
            throw new ValidationException("User has not completed a booking for this item");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(UserMapper.toUser(user));
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
    }


    private void checkOwner(Integer ownerId) {
        if (ownerId == null || ownerId < 1) {
            throw new ValidationException("OwnerId should not be not empty and positive");
        }
        userService.getUserById(ownerId);
    }

    private Item checkId(Integer itemId) {
        if (itemId == null || itemId < 1) {
            throw new ValidationException("Id should be not empty and positive");
        }
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
