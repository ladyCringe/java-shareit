package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {

    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private UserDto userDto;
    private User user;
    private UserDto anotherUserDto;
    private User anotherUser;

    @BeforeEach
    void init() {
        userDto = userService.createUser(new UserDto(null, "Thorfinn", "thorfinn@vinland.com"));
        user = new User(userDto.getId(), userDto.getName(), userDto.getEmail());

        anotherUserDto = userService.createUser(new UserDto(null, "Askeladd", "askeladd@wales.com"));
        anotherUser = new User(anotherUserDto.getId(), anotherUserDto.getName(), anotherUserDto.getEmail());
    }

    @Test
    void userCanCreateRequest() {
        ItemRequestDto request = new ItemRequestDto(null, "Seeking a boat to reach Vinland", LocalDateTime.now());
        ItemRequestResponseDto saved = itemRequestService.createRequest(user.getId(), request);

        Assertions.assertThat(saved.getId()).isNotNull();
        Assertions.assertThat(saved.getDescription()).isEqualTo("Seeking a boat to reach Vinland");
    }

    @Test
    void userCanNotFindsHisOwnRequests() {
        ItemRequestResponseDto req1 = itemRequestService.createRequest(user.getId(),
                new ItemRequestDto(null, "Need a sword without blood", LocalDateTime.now()));
        ItemRequestResponseDto req2 = itemRequestService.createRequest(user.getId(),
                new ItemRequestDto(null, "Looking for peace treaty scrolls", LocalDateTime.now()));

        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(user.getId());

        Assertions.assertThat(requests).hasSize(0);
        Assertions.assertThat(requests.isEmpty()).isTrue();
    }

    @Test
    void userCanFindsOthersRequests() {
        ItemRequestResponseDto req1 = itemRequestService.createRequest(user.getId(),
                new ItemRequestDto(null, "Need a sword without blood", LocalDateTime.now()));
        ItemRequestResponseDto req2 = itemRequestService.createRequest(user.getId(),
                new ItemRequestDto(null, "Looking for peace treaty scrolls", LocalDateTime.now()));
        ItemRequestResponseDto req3 = itemRequestService.createRequest(anotherUser.getId(),
                new ItemRequestDto(null, "Looking for another world", LocalDateTime.now()));


        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(anotherUser.getId());

        Assertions.assertThat(requests).hasSize(2);
        Assertions.assertThat(requests.contains(req1)).isTrue();
        Assertions.assertThat(requests.contains(req2)).isTrue();

        requests = itemRequestService.getAllRequests(user.getId());

        Assertions.assertThat(requests.contains(req3)).isTrue();
    }

    @Test
    void userCanSeeOthersRequests() {
        itemRequestService.createRequest(user.getId(),
                new ItemRequestDto(null, "Requesting holy scriptures", LocalDateTime.now()));
        itemRequestService.createRequest(user.getId(),
                new ItemRequestDto(null, "In need of royal cloak", LocalDateTime.now()));
        itemRequestService.createRequest(anotherUser.getId(),
                new ItemRequestDto(null, "Strategy scrolls", LocalDateTime.now()));

        List<ItemRequestResponseDto> results = itemRequestService.getAllRequests(anotherUser.getId());

        Assertions.assertThat(results).hasSize(2);
        Assertions.assertThat(results).noneMatch(r -> r.getDescription().equals("Strategy scrolls"));
    }

    @Test
    void userFindsRequestByIdCorrectly() {
        ItemRequestDto request = new ItemRequestDto(null,
                "Need maps of new lands", LocalDateTime.now());
        ItemRequestResponseDto created = itemRequestService.createRequest(anotherUser.getId(), request);
        ItemRequestResponseDto found = itemRequestService.getRequestById(created.getId());

        Assertions.assertThat(found.getId()).isEqualTo(created.getId());
        Assertions.assertThat(found.getDescription()).isEqualTo("Need maps of new lands");
    }

    @Test
    void userThrowsExceptionIfUserNotFound() {
        Assertions.assertThatThrownBy(() ->
                itemRequestService.getRequestsByUserId(999)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void userHasNoRequestsInitially() {
        List<ItemRequestResponseDto> requests = itemRequestService.getRequestsByUserId(anotherUserDto.getId());

        Assertions.assertThat(requests).isEmpty();
    }
}
