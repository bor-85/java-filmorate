package ru.yandex.practicum.filmorate.storage;

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
public class FilmDbStorage extends StorageBaseOperations<Film> implements ru.yandex.practicum.filmorate.storage.FilmStorage {

    private static final String FILM_WITH_GENRES_MPA_BASE =
            "SELECT " +
                    "  f.id AS film_id, " +
                    "  f.name AS film_name, " +
                    "  f.description AS film_description, " +
                    "  f.releaseDate AS film_release_date, " +
                    "  f.duration AS film_duration, " +
                    "  m.id AS mpa_id, " +
                    "  m.name AS mpa_name, " +
                    "  g.id AS genre_id, " +
                    "  g.name AS genre_name " +
                    "FROM films f " +
                    "JOIN mparating m ON m.id = f.mparating_id " +
                    "LEFT JOIN film_genre fg ON fg.film_id = f.id " +
                    "LEFT JOIN genres g ON g.id = fg.genre_id ";

    private static final String FIND_BY_ID_QUERY = FILM_WITH_GENRES_MPA_BASE + "WHERE f.id = ?";

    private static final String FIND_ALL_QUERY = FILM_WITH_GENRES_MPA_BASE + "ORDER BY f.id, g.id";

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
                    "  m.name AS mpa_name, " +
                    "  g.id AS genre_id, " +
                    "  g.name AS genre_name " +
                    "FROM popular p " +
                    "JOIN films f ON f.id = p.film_id " +
                    "LEFT JOIN mparating m ON m.id = f.mparating_id " +
                    "LEFT JOIN film_genre fg ON fg.film_id = f.id " +
                    "LEFT JOIN genres g ON g.id = fg.genre_id " +
                    "ORDER BY p.likes_count DESC, f.id, g.id";

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

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    private static Timestamp toTimestamp(java.time.LocalDate date) {
        return date == null ? null : Timestamp.valueOf(date.atStartOfDay());
    }

    private static Set<Long> extractGenreIds(Film film) {
        if (film.getGenres() == null) {
            return Set.of();
        }
        return film.getGenres().stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static Long extractMpaId(Film film) {
        if (film.getMpa() == null) {
            return null;
        }
        MpaRating mpa = film.getMpa();
        return mpa.getId();
    }

    private static List<Film> aggregateFilms(List<Film> rows) {
        Map<Long, Film> map = new LinkedHashMap<>();

        for (Film row : rows) {
            Long filmId = row.getId();
            if (filmId == null) continue;

            Film existing = map.get(filmId);
            if (existing == null) {
                row.setGenres(row.getGenres() == null ? new HashSet<>() : new HashSet<>(row.getGenres()));
                map.put(filmId, row);
            } else {
                if (row.getGenres() != null) {
                    existing.getGenres().addAll(row.getGenres());
                }
            }
        }

        return new ArrayList<>(map.values());
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

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> rows = findMany(FIND_BY_ID_QUERY, id);
        List<Film> films = aggregateFilms(rows);
        return films.stream().findFirst();
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> rows = findMany(FIND_ALL_QUERY);
        return aggregateFilms(rows);
    }

    @Override
    @Transactional
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY,
                filmId, userId,
                filmId, userId
        );
    }

    @Override
    @Transactional
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        if (count <= 0) {
            return List.of();
        }
        List<Film> rows = findMany(POPULAR_QUERY, count);
        return aggregateFilms(rows);
    }
}