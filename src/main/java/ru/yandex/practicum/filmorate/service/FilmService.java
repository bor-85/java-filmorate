package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserHandleMessages;

import java.util.*;

import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.ERROR_ID_NOT_FOUND;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));
    }

    public void addLike(Long film_id, Long user_id) {
        checkFilmId(film_id);
        checkUserId(user_id);

        filmStorage.addLike(film_id, user_id);
    }

    public void removeLike(Long film_id, Long user_id) {
        checkFilmId(film_id);
        checkUserId(user_id);

        filmStorage.removeLike(film_id, user_id);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Film f) -> filmStorage.getLikeCount(f.getId()))
                        .reversed()
                        .thenComparing(Film::getId))
                .limit(count)
                .toList();
    }

    private void checkFilmId(Long filmId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException(ERROR_ID_NOT_FOUND + filmId);
        }
    }

    private void checkUserId(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException(UserHandleMessages.ERROR_ID_NOT_FOUND + userId);
        }
    }

}
