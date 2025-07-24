package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(@Valid UserDto userDto);

    UserDto updateUser(Integer id, UserDto userDto);

    List<UserDto> getAllUsers();

    UserDto getUserById(Integer id);

    void deleteUser(Integer id);
}
