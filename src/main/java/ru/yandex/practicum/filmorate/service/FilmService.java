package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.ERROR_ID_NOT_FOUND;
import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.ERROR_ID_NOT_FOUND_MPA;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaService mpaService,
                       GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public List<FilmDto> findAll() {
        log.debug("Service: findAll() filmsCount={}", filmStorage.findAll().size());
        return filmStorage.findAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto add(NewFilmRequest request) {
        log.info("Service: add film name='{}', releaseDate={}, duration={}",
                request.getName(), request.getReleaseDate(), request.getDuration());
        Long mpaId = (request.getMpa() == null) ? null : request.getMpa().getId();
        if (mpaId == null) {
            throw new NotFoundException(ERROR_ID_NOT_FOUND_MPA + mpaId);
        }
        mpaService.findById(mpaId);

        if (request.getGenres() != null) {
            for (GenreDto g : request.getGenres()) {
                if (g != null && g.getId() != null) {
                    genreService.findById(g.getId());
                }
            }
        }

        Film film = FilmMapper.mapToFilm(request);
        Film created = filmStorage.add(film);

        log.info("Service: film created id={}, name='{}'", created.getId(), created.getName());
        return filmStorage.findById(created.getId())
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + created.getId()));
    }

    public FilmDto findById(Long id) {
        log.debug("Service: findById id={}", id);
        return filmStorage.findById(id)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));
    }

    public FilmDto update(Long filmId, UpdateFilmRequest request) {
        log.info("Service: update film id={}, name='{}'", filmId, request.getName());

        Film oldFilm = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + filmId));
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            mpaService.findById(request.getMpa().getId());
        }

        if (request.getGenres() != null) {
            for (GenreDto g : request.getGenres()) {
                if (g != null && g.getId() != null) {
                    genreService.findById(g.getId());
                }
            }
        }

        FilmMapper.updateFilmFields(oldFilm, request);

        Film updated = filmStorage.update(oldFilm);

        log.info("Service: film updated id={}, name='{}'", updated.getId(), updated.getName());
        return filmStorage.findById(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + filmId));
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

    public List<FilmDto> getPopular(int count) {
        log.debug("Service: getPopular count={}", count);

        return filmStorage.getPopular(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
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
            throw new NotFoundException(ru.yandex.practicum.filmorate.validation.UserHandleMessages.ERROR_ID_NOT_FOUND + userId);
        }
    }
}