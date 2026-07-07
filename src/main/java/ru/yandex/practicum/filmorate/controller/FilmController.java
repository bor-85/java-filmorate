package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("GET /films");
        var result = filmService.findAll();
        log.debug("GET /films -> {} films", result.size());
        return result;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films request: name={}, releaseDate={}, duration={}",
                film.getName(), film.getReleaseDate(), film.getDuration());

        Film created = filmService.add(film);
        log.info("POST /films created: id={}, name={}", film.getId(), film.getName());
        return created;
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.debug("GET /films/{}", id);
        return filmService.findById(id);
    }

    //PUT /films/{id}/like/{userId}
    @PutMapping("/{id}/like/{userId}")
    public void addlike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);

        log.debug("Like added filmId={} userId={}", id, userId);
    }

    // DELETE /films/{id}/like/{userId}
    @DeleteMapping("/{id}/like/{userId}")
    public void removelike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("DELETE /films/{}/like/{}", id, userId);
        filmService.removeLike(id, userId);

        log.debug("Like removed filmId={} userId={}", id, userId);
    }

    // GET /films/popular?count={count}
    @GetMapping("/popular")
    public Collection<Film> popular(@RequestParam(defaultValue = "10") int count) {
        log.debug("GET /films/popular?count={}", count);

        var result = filmService.getPopular(count);

        log.debug("Popular films fetched: {}", result.size());
        return result;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films request: id={}, name={}", newFilm.getId(), newFilm.getName());

        if (newFilm.getId() == null) {
            log.warn("PUT /films rejected: id is null");
            throw new ValidationException(ERROR_ID_IS_NULL);
        }

        return filmService.update(newFilm);
    }
}
