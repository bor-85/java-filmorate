package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage extends StorageBaseOperations<Genre> implements GenreStorage {

    private static final String FIND_GENRES_BY_FILM_IDS =
            "SELECT fg.film_id AS film_id, g.id AS id, g.name AS name " +
                    "FROM film_genre fg " +
                    "JOIN genres g ON g.id = fg.genre_id " +
                    "WHERE fg.film_id IN (%s) " +
                    "ORDER BY fg.film_id, g.id";

    private static final String FIND_ALL_QUERY =
            "SELECT id, name FROM genres ORDER BY id";

    private static final String FIND_BY_ID_QUERY =
            "SELECT id, name FROM genres WHERE id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Map<Long, Set<Genre>> findGenresByFilmIds(Collection<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = filmIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        String placeholders = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = String.format(FIND_GENRES_BY_FILM_IDS, placeholders);

        List<Map<String, Object>> rows = jdbc.queryForList(sql, ids.toArray());

        Map<Long, Set<Genre>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long genreId = ((Number) row.get("id")).longValue();
            String genreName = (String) row.get("name");

            result.computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(new Genre() {{
                        setId(genreId);
                        setName(genreName);
                    }
                    });
        }
        return result;
    }

}