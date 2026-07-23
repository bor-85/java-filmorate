package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.ERROR_ID_IS_NULL;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmDto> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest request) {
        return filmService.add(request);
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable Long id) {
        return filmService.findById(id);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest request) {
        if (request.getId() == null) {
            throw new ValidationException(ERROR_ID_IS_NULL);
        }
        return filmService.update(request.getId(), request);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> popular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }
}