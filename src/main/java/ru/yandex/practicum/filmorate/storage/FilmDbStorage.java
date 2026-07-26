package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage extends StorageBaseOperations<Film> implements FilmStorage {

    private static final String FILM_WITH_MPA_BASE =
            "SELECT " +
                    "  f.id AS film_id, " +
                    "  f.name AS film_name, " +
                    "  f.description AS film_description, " +
                    "  f.releaseDate AS film_release_date, " +
                    "  f.duration AS film_duration, " +
                    "  m.id AS mpa_id, " +
                    "  m.name AS mpa_name " +
                    "FROM films f " +
                    "JOIN mparating m ON m.id = f.mparating_id ";

    private static final String FIND_BY_ID_QUERY = FILM_WITH_MPA_BASE + "WHERE f.id = ?";
    private static final String FIND_ALL_QUERY = FILM_WITH_MPA_BASE + "ORDER BY f.id";

    private static final String POPULAR_QUERY =
            "WITH popular AS ( " +
                    "  SELECT f.id AS film_id, COUNT(l.user_id) AS likes_count " +
                    "  FROM films f " +
                    "  LEFT JOIN likes l ON l.film_id = f.id " +
                    "  GROUP BY f.id " +
                    "  ORDER BY likes_count DESC, f.id " +
                    "  LIMIT ? " +
                    ") " +
                    "SELECT " +
                    "  f.id AS film_id, " +
                    "  f.name AS film_name, " +
                    "  f.description AS film_description, " +
                    "  f.releaseDate AS film_release_date, " +
                    "  f.duration AS film_duration, " +
                    "  m.id AS mpa_id, " +
                    "  m.name AS mpa_name " +
                    "FROM popular p " +
                    "JOIN films f ON f.id = p.film_id " +
                    "JOIN mparating m ON m.id = f.mparating_id " +
                    "ORDER BY p.likes_count DESC, f.id";

    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films(name, description, releaseDate, duration, mparating_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM_QUERY =
            "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mparating_id = ? WHERE id = ?";

    private static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genre WHERE film_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY =
            "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";

    private static final String ADD_LIKE_QUERY =
            "INSERT INTO likes(film_id, user_id) " +
                    "SELECT ?, ? " +
                    "WHERE NOT EXISTS ( " +
                    "  SELECT 1 FROM likes WHERE film_id = ? AND user_id = ? " +
                    ")";

    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    public FilmDbStorage(JdbcTemplate jdbc,
                         @Qualifier("filmRowMapperWithoutGenres") RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    private static Timestamp toTimestamp(java.time.LocalDate date) {
        return date == null ? null : Timestamp.valueOf(date.atStartOfDay());
    }

    private static Set<Long> extractGenreIds(Film film) {
        if (film.getGenres() == null) return Set.of();
        return film.getGenres().stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static Long extractMpaId(Film film) {
        if (film.getMpa() == null) return null;
        MpaRating mpa = film.getMpa();
        return mpa.getId();
    }

    @Override
    @Transactional
    public Film add(Film film) {
        long filmId = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                toTimestamp(film.getReleaseDate()),
                film.getDuration(),
                extractMpaId(film)
        );

        film.setId(filmId);

        for (Long genreId : extractGenreIds(film)) {
            jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genreId);
        }

        film.setGenres(Set.of());

        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                toTimestamp(film.getReleaseDate()),
                film.getDuration(),
                extractMpaId(film),
                film.getId()
        );

        jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());
        for (Long genreId : extractGenreIds(film)) {
            jdbc.update(INSERT_FILM_GENRE_QUERY, film.getId(), genreId);
        }

        film.setGenres(Set.of());
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    @Transactional
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId, filmId, userId);
    }

    @Override
    @Transactional
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        if (count <= 0) return List.of();
        return findMany(POPULAR_QUERY, count);
    }
}