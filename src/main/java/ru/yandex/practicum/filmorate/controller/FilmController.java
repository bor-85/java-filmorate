package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final String ERROR_ID_IS_NULL = "Id должен быть указан";
    private static final String ERROR_ID_NOT_FOUND = "Не найден фильм с id = ";

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("GET /films -> count={}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films request: name={}, releaseDate={}, duration={}",
                film.getName(), film.getReleaseDate(), film.getDuration());

        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        log.info("POST /films created: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    // Генерация идентификатора
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long nextId = currentMaxId + 1;
        log.debug("Generated nextFilmId={}", nextId);
        return nextId;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films request: id={}, name={}", newFilm.getId(), newFilm.getName());

        if (newFilm.getId() == null) {
            log.warn("PUT /films rejected: id is null");
            throw new ValidationException(ERROR_ID_IS_NULL);
        }

        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("PUT /films rejected: film not found, id={}", newFilm.getId());
            throw new NotFoundException(ERROR_ID_NOT_FOUND + newFilm.getId());
        }

        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setName(newFilm.getName());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());

        log.info("PUT /films updated: id={}, name={}", oldFilm.getId(), oldFilm.getName());
        return oldFilm;
    }
}
