package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import static ru.yandex.practicum.filmorate.validation.UserHandleMessages.*;

import java.util.*;


@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        checkUserId(userId);

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        return userStorage.getCommonFriends(userId, friendId);
    }

    private void checkUserId(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException(ERROR_ID_NOT_FOUND + userId);
        }
    }

}
