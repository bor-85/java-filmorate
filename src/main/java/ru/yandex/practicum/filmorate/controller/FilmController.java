package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.*;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.add(film);
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        return filmService.findById(id);
    }

    //PUT /films/{id}/like/{userId}
    @PutMapping("/{id}/like/{userId}")
    public void addlike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    // DELETE /films/{id}/like/{userId}
    @DeleteMapping("/{id}/like/{userId}")
    public void removelike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    // GET /films/popular?count={count}
    @GetMapping("/popular")
    public Collection<Film> popular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException(ERROR_ID_IS_NULL);
        }
        return filmService.update(newFilm);
    }
}
