package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import static ru.yandex.practicum.filmorate.validation.UserHandleMessages.*;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        log.debug("Service: findAll usersCount={}", userStorage.findAll().size());
        return userStorage.findAll();
    }

    public User add(User user) {
        log.info("Service: add user login='{}', email='{}', birthday={}",
                user.getLogin(), user.getEmail(), user.getBirthday());

        normalizeName(user);

        User created = userStorage.add(user);

        log.info("Service: user created id={}, login='{}'", created.getId(), created.getLogin());
        return created;
    }

    public User findById(Long id) {
        log.debug("Service: findById id={}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));
    }

    public User update(User newUser) {
        Long id = newUser.getId();
        log.info("Service: update user id={}, login='{}'", id, newUser.getLogin());

        User oldUser = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));

        normalizeName(newUser);
        setUserFields(oldUser, newUser);

        User updated = userStorage.update(oldUser);

        log.info("Service: user updated id={}, login='{}'", updated.getId(), updated.getLogin());
        return updated;
    }

    public void addFriend(Long userId, Long friendId) {
        log.debug("Service: addFriend userId={}, friendId={}", userId, friendId);

        checkUserId(userId);
        checkUserId(friendId);

        userStorage.addFriend(userId, friendId);

        log.info("Service: friendship added userId={}, friendId={}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.debug("Service: removeFriend userId={}, friendId={}", userId, friendId);

        checkUserId(userId);
        checkUserId(friendId);

        userStorage.removeFriend(userId, friendId);

        log.info("Service: friendship removed userId={}, friendId={}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.debug("Service: getFriends userId={}", userId);

        checkUserId(userId);

        var friends = userStorage.getFriends(userId);
        log.info("Service: getFriends userId={} count={}", userId, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        log.debug("Service: getCommonFriends userId={}, otherId={}", userId, friendId);

        checkUserId(userId);
        checkUserId(friendId);

        var common = userStorage.getCommonFriends(userId, friendId);
        log.info("Service: getCommonFriends userId={} otherId={} count={}", userId, friendId, common.size());
        return common;
    }

    private void checkUserId(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("Service: user not found id={}", userId);
            throw new NotFoundException(ERROR_ID_NOT_FOUND + userId);
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Service: normalizeName: name is blank -> set from login, login='{}'", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private void setUserFields(User oldUser, User newUser) {
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
    }

}
