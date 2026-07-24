package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validation.UserHandleMessages.ERROR_ID_IS_NULL;
import static ru.yandex.practicum.filmorate.validation.UserHandleMessages.ERROR_ID_NOT_FOUND;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserDto> findAll() {
        log.debug("Service: findAll usersCount={}", userStorage.findAll().size());
        return userStorage.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Transactional
    public UserDto add(NewUserRequest request) {
        log.info("Service: add user login='{}', email='{}', birthday={}",
                request.getLogin(), request.getEmail(), request.getBirthday());

        User user = UserMapper.mapToUser(request);
        normalizeName(user);

        User created = userStorage.add(user);

        log.info("Service: user created id={}, login='{}'", created.getId(), created.getLogin());
        return UserMapper.mapToUserDto(created);
    }

    public UserDto findById(Long id) {
        log.debug("Service: findById id={}", id);
        return userStorage.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));
    }

    @Transactional
    public UserDto update(Long userId, UpdateUserRequest request) {
        if (userId == null) {
            throw new ValidationException(ERROR_ID_IS_NULL);
        }

        log.info("Service: update user id={}, login='{}'", userId, request.getLogin());

        User oldUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + userId));

        UserMapper.updateUserFields(oldUser, request);
        normalizeName(oldUser);

        User updated = userStorage.update(oldUser);

        log.info("Service: user updated id={}, login='{}'", updated.getId(), updated.getLogin());
        return UserMapper.mapToUserDto(updated);
    }

    @Transactional
    public void addFriend(Long userId, Long friendId) {
        log.debug("Service: addFriend userId={}, friendId={}", userId, friendId);

        checkUserId(userId);
        checkUserId(friendId);

        userStorage.addFriend(userId, friendId);

        log.info("Service: friendship added userId={}, friendId={}", userId, friendId);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Service: removeFriend userId={}, friendId={}", userId, friendId);

        checkUserId(userId);
        checkUserId(friendId);

        userStorage.removeFriend(userId, friendId);

        log.info("Service: friendship removed userId={}, friendId={}", userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        log.debug("Service: getFriends userId={}", userId);
        checkUserId(userId);

        return userStorage.getFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        log.debug("Service: getCommonFriends userId={}, otherId={}", userId, friendId);
        checkUserId(userId);
        checkUserId(friendId);

        return userStorage.getCommonFriends(userId, friendId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    private void checkUserId(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("Service: user not found id={}", userId);
            throw new NotFoundException(ERROR_ID_NOT_FOUND + userId);
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}