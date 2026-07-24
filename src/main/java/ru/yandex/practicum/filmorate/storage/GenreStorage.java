package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {
    List<Genre> findAll();

    Optional<Genre> findById(Long id);

    Map<Long, Set<Genre>> findGenresByFilmIds(Collection<Long> filmIds);

    Set<Long> findExistingGenreIds(Collection<Long> ids);
}