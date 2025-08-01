package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    private UserDto eren;
    private UserDto mikasa;

    @BeforeEach
    void init() {
        eren = new UserDto(null, "Eren Yeager", "eren@paradis.com");
        mikasa = new UserDto(null, "Mikasa Ackerman", "mikasa@paradis.com");
    }

    @Test
    void createUserTest() {
        UserDto result = userService.createUser(eren);

        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo(eren.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(eren.getEmail());
    }

    @Test
    void updateUserTest() {
        UserDto created = userService.createUser(eren);
        UserDto patch = new UserDto(null, "Founding Titan", "attack@titan.org");

        UserDto updated = userService.updateUser(created.getId(), patch);

        Assertions.assertThat(updated.getId()).isEqualTo(created.getId());
        Assertions.assertThat(updated.getName()).isEqualTo("Founding Titan");
        Assertions.assertThat(updated.getEmail()).isEqualTo("attack@titan.org");
    }

    @Test
    void getUserByIdTest() {
        UserDto saved = userService.createUser(mikasa);
        UserDto fetched = userService.getUserById(saved.getId());

        Assertions.assertThat(fetched).usingRecursiveComparison().isEqualTo(saved);
    }

    @Test
    void getAllUsersTest() {
        userService.createUser(eren);
        userService.createUser(mikasa);

        List<UserDto> all = userService.getAllUsers();

        Assertions.assertThat(all).hasSize(2);
    }

    @Test
    void deleteUserTest() {
        UserDto saved = userService.createUser(eren);
        userService.deleteUser(saved.getId());

        Assertions.assertThatThrownBy(() -> userService.getUserById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createUserDuplicateEmail() {
        userService.createUser(mikasa);
        UserDto duplicate = new UserDto(null, "Another", "mikasa@paradis.com");

        Assertions.assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(ConflictException.class);
    }
}
