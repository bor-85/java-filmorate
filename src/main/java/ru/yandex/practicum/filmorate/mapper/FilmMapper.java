package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(request.getMpa().getId());
            film.setMpa(mpa);
        }

        film.setGenres(toGenres(request.getGenres()));
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        if (film == null) {
            return null;
        }

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getMpa() != null) {
            MpaaRatingDto mpaDto = new MpaaRatingDto();
            mpaDto.setId(film.getMpa().getId());
            mpaDto.setName(film.getMpa().getName());
            dto.setMpa(mpaDto);
        } else {
            dto.setMpa(null);
        }

        Set<GenreDto> genres = film.getGenres() == null
                ? Collections.emptySet()
                : film.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .map(g -> {
                    GenreDto gd = new GenreDto();
                    gd.setId(g.getId());
                    gd.setName(g.getName());
                    return gd;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        dto.setGenres(genres);

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.getName() != null) {
            film.setName(request.getName());
        }
        if (request.getDescription() != null) {
            film.setDescription(request.getDescription());
        }
        if (request.getReleaseDate() != null) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.getDuration() != null) {
            film.setDuration(request.getDuration());
        }

        if (request.hasMpa()) {
            MpaRating mpa = new MpaRating();
            mpa.setId(request.getMpa().getId());
            film.setMpa(mpa);
        }

        if (request.getGenres() != null) {
            film.setGenres(toGenres(request.getGenres()));
        }

        return film;
    }

    private static Set<Genre> toGenres(Set<GenreDto> genreDtos) {
        if (genreDtos == null) {
            return null;
        }

        return genreDtos.stream()
                .filter(g -> g != null && g.getId() != null)
                .map(g -> {
                    Genre genre = new Genre();
                    genre.setId(g.getId());
                    return genre;
                })
                .collect(Collectors.toSet());
    }
}