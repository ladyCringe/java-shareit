package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@Service
public class UserServiceImpl implements UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    private int getNextId() {
        return nextId++;
    }

    @Override
    public UserDto createUser(@Valid UserDto  userDto) {
        checkEmail(userDto);
        User user = UserMapper.toUser(userDto);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Integer id, UserDto userDto) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User with id = " + id + " was not found");
        }
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new ConflictException("Email already in use: " + userDto.getEmail());
        }
        validateUpd(userDto);
        User user = users.get(id);
        update(user, userDto);
        users.put(user.getId(), user);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public UserDto  getUserById(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User with id = " + id + " was not found");
        }
        User user = users.get(id);
        return UserMapper.toDto(user);
    }

    @Override
    public void deleteUser(Integer id) {
        users.remove(id);
    }

    private void validateUpd(UserDto user) {
        if (user.getEmail() == null && user.getName() == null) {
            throw new ValidationException("Updated user should not be empty");
        }
    }

    private void checkEmail(UserDto user) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new ConflictException("Email already in use: " + user.getEmail());
        }
    }

    private void update(User user, UserDto userDto) {
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
    }
}
