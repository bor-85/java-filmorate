package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import static ru.yandex.practicum.filmorate.validation.UserHandleMessages.*;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.debug("GET /users");
        var result = userService.findAll();
        log.debug("GET /users -> count={}", result.size());
        return result;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users request: login={}, email={}, birthday={}",
                user.getLogin(), user.getEmail(), user.getBirthday());

        User created = userService.add(user);
        log.info("POST /users created: id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    // GET /users/{id}
    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        log.debug("GET /users/{}", id);
        return userService.findById(id);
    }

    // PUT /users/{id}/friends/{friendId}
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("PUT /users/{}/friends/{}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Friend added: userId={} friendId={}", id, friendId);
    }

    // DELETE /users/{id}/friends/{friendId}
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("DELETE /users/{}/friends/{}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Friend removed: userId={} friendId={}", id, friendId);
    }

    // GET /users/{id}/friends
    @GetMapping("/{id}/friends")
    public Collection<User> friends(@PathVariable Long id) {
        log.debug("GET /users/{}/friends", id);

        var result = userService.getFriends(id);
        log.debug("GET /users/{}/friends -> count={}", id, result.size());
        return result;
    }

    // GET /users/{id}/friends/common/{otherId}
    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> commonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("GET /users/{}/friends/common/{}", id, otherId);

        var result = userService.getCommonFriends(id, otherId);
        log.debug("GET /users/{}/friends/common/{} -> count={}", id, otherId, result.size());
        return result;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("PUT /users request: id={}, login={}", newUser.getId(), newUser.getLogin());

        if (newUser.getId() == null) {
            log.warn("PUT /users rejected: id is null");
            throw new ValidationException(ERROR_ID_IS_NULL);
        }

        return userService.update(newUser);
    }
}
