package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserServiceImpl implements UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    private int getNextId() {
        return nextId++;
    }

    @Override
    public UserDto createUser(UserDto  userDto) {
        User user = UserMapper.toUser(userDto);
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new ConflictException("Email already in use: " + userDto.getEmail());
        }
        validate(user);
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
        User user = users.get(id);
        checkUser(user.getId());
        validate(user);
        UserMapper.update(user, userDto);
        users.put(user.getId(), user);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream().map(UserMapper::toDto).toList();
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
        checkUser(id);
        users.remove(id);
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email should not be empty and must contain @");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("Name should not be empty and must contain @");
        }
    }

    private void checkUser(int userId) {
        if (getUserById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }
}
