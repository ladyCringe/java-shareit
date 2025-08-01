package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("New request to create user: {}", userDto);
        UserDto createdUser = userService.createUser(userDto);
        log.info("New user created: {}", createdUser);
        return createdUser;
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        log.info("New request to update user: {}", userDto);
        UserDto updatedUser = userService.updateUser(id, userDto);
        log.info("User updated: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Display a list of all users");
        List<UserDto> users = userService.getAllUsers();
        log.info("The list successfully displayed");
        return users;
    }

    @GetMapping("/{id}")
    public UserDto  getUserById(@PathVariable(name = "id") Integer userId) {
        log.info("New getUserById request for user with id {}.", userId);
        UserDto  user = userService.getUserById(userId);
        log.info("User with id {} successfully displayed.", userId);
        return user;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable(name = "id") Integer userId) {
        log.info("New deleteUser request for user with id {}.", userId);
        userService.deleteUser(userId);
        log.info("User with id {} successfully deleted.", userId);
    }
}
