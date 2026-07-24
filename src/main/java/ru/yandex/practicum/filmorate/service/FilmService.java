package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.*;

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
        var filmsFromDb = filmStorage.findAll();

        log.debug("Service: findAll() filmsCount={}", filmsFromDb.size());

        List<Film> films = new ArrayList<>(filmsFromDb);
        fillGenres(films);

        return films.stream()
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

        validateGenresExist(request.getGenres());

        Film film = FilmMapper.mapToFilm(request);
        Film created = filmStorage.add(film);

        fillGenres(Collections.singletonList(created));

        log.info("Service: film created id={}, name='{}'", created.getId(), created.getName());
        return FilmMapper.mapToFilmDto(created);
    }

    public FilmDto findById(Long id) {
        log.debug("Service: findById id={}", id);

        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + id));

        fillGenres(Collections.singletonList(film));

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(Long filmId, UpdateFilmRequest request) {
        if (filmId == null) {
            throw new ValidationException(ERROR_ID_IS_NULL);
        }

        log.info("Service: update film id={}, name='{}'", filmId, request.getName());

        Film oldFilm = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND + filmId));

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            mpaService.findById(request.getMpa().getId());
        }

        validateGenresExist(request.getGenres());

        FilmMapper.updateFilmFields(oldFilm, request);
        Film updated = filmStorage.update(oldFilm);

        fillGenres(Collections.singletonList(updated));

        log.info("Service: film updated id={}, name='{}'", updated.getId(), updated.getName());
        return FilmMapper.mapToFilmDto(updated);
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

        List<Film> films = filmStorage.getPopular(count);
        fillGenres(films);

        return films.stream()
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

    private void validateGenresExist(Set<GenreDto> requestGenres) {
        if (requestGenres == null) {
            return;
        }

        Set<Long> ids = requestGenres.stream()
                .filter(Objects::nonNull)
                .map(GenreDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return;
        }

        Set<Long> existingIds = genreService.findExistingGenreIds(ids);

        ids.removeAll(existingIds);

        if (!ids.isEmpty()) {
            Long missingId = ids.iterator().next();
            throw new NotFoundException(ERROR_ID_NOT_FOUND_GENRE + missingId);
        }
    }

    private void fillGenres(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (filmIds.isEmpty()) {
            return;
        }

        Map<Long, Set<Genre>> genresByFilmId = genreService.getGenresByFilmIds(filmIds);

        for (Film film : films) {
            Long id = film.getId();
            Set<Genre> genres = (id == null)
                    ? Set.of()
                    : genresByFilmId.getOrDefault(id, Set.of());

            Set<Genre> sorted = genres.stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            film.setGenres(sorted);
        }
    }
}