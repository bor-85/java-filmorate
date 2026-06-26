package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private static final String ERROR_ID_IS_NULL = "Id должен быть указан";
    private static final String ERROR_ID_NOT_FOUND = "Не найден пользователь с id = ";

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 0;

    @GetMapping
    public Collection<User> findAll() {
        log.debug("GET /users -> count={}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users request: login={}, email={}, birthday={}",
                user.getLogin(), user.getEmail(), user.getBirthday());

        normalizeName(user);

        long id = ++nextId;
        user.setId(id);
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        log.info("POST /users created: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("PUT /users request: id={}, login={}", newUser.getId(), newUser.getLogin());
        if (newUser.getId() == null) {
            log.warn("PUT /users rejected: id is null");
            throw new ValidationException(ERROR_ID_IS_NULL);
        }

        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("PUT /users rejected: user not found, id={}", newUser.getId());
            throw new NotFoundException(ERROR_ID_NOT_FOUND + newUser.getId());
        }
        normalizeName(newUser);

        setUserFields(oldUser, newUser);

        log.info("PUT /users updated: id={}, login={}", oldUser.getId(), oldUser.getLogin());
        return oldUser;
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
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
