package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    Collection<Film> findAll();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    int getLikeCount(Long filmId);
}
