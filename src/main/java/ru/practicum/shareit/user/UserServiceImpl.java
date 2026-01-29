package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private long idCounter = 0;

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (emails.contains(userDto.getEmail())) {
            throw new ConflictException("Email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(++idCounter);

        users.put(user.getId(), user);
        emails.add(user.getEmail());

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (emails.contains(userDto.getEmail())) {
                throw new ConflictException("Email уже используется");
            }
            emails.remove(user.getEmail());
            emails.add(userDto.getEmail());
        }

        UserMapper.updateUserFromDto(userDto, user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = users.remove(id);
        if (user != null) {
            emails.remove(user.getEmail());
        }
    }

}
