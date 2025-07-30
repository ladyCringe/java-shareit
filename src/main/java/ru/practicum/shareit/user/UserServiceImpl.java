package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto  userDto) {
        checkEmail(userDto);
        User user = UserMapper.toUser(userDto);
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto updateUser(Integer id, UserDto userDto) {
        validateUpd(userDto);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " was not found"));
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            checkEmail(userDto);
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public UserDto  getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " was not found"));
        return UserMapper.toDto(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    private void validateUpd(UserDto user) {
        if (user.getEmail() == null && user.getName() == null) {
            throw new ValidationException("Updated user should not be empty");
        }
    }

    private void checkEmail(UserDto user) {
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        if (exists) {
            throw new ConflictException("Email already in use: " + user.getEmail());
        }
    }
}
