package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmHandleMessages;
import ru.yandex.practicum.filmorate.validation.UserHandleMessages;

import java.util.*;

import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.ERROR_ID_NOT_FOUND;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        log.debug("Service: findAll() filmsCount={}", filmStorage.findAll().size());
        return filmStorage.findAll();
    }

    public Film add(Film film) {
        log.info("Service: add film name='{}', releaseDate={}, duration={}",
                film.getName(), film.getReleaseDate(), film.getDuration());

        Film created = filmStorage.add(film);

        log.info("Service: film created id={}, name='{}'", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        Long id = film.getId();
        log.info("Service: update film id={}, name='{}'", id, film.getName());

        Film oldFilm = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(FilmHandleMessages.ERROR_ID_NOT_FOUND + id));

        setFilmFields(oldFilm, film);

        Film updated = filmStorage.update(oldFilm);

        log.info("Service: film updated id={}, name='{}'", updated.getId(), updated.getName());
        return updated;
    }

    public Film findById(Long id) {
        log.debug("Service: findById id={}", id);

        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("Service: addLike filmId={}, userId={}", filmId, userId);
        checkFilmId(filmId);
        checkUserId(userId);

        filmStorage.addLike(filmId, userId);

        log.info("Service: like added filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("Service: removeLike filmId={}, userId={}", filmId, userId);

        checkFilmId(filmId);
        checkUserId(userId);

        filmStorage.removeLike(filmId, userId);

        log.info("Service: like removed filmId={}, userId={}", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        log.debug("Service: getPopular count={}", count);

        var result = filmStorage.getPopular(count);

        log.info("Service: popular films fetched count={}, resultSize={}", count, result.size());
        return result;
    }

    private void checkFilmId(Long filmId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            log.warn("Service: film not found id={}", filmId);
            throw new NotFoundException(ERROR_ID_NOT_FOUND + filmId);
        }
    }

    private void checkUserId(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("Service: user not found id={}", userId);
            throw new NotFoundException(UserHandleMessages.ERROR_ID_NOT_FOUND + userId);
        }
    }

    private void setFilmFields(Film oldFilm, Film newFilm) {
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setName(newFilm.getName());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
    }

}
