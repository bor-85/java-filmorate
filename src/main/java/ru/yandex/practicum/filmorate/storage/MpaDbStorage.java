package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends StorageBaseOperations<MpaRating> implements MpaStorage {

    private static final String FIND_ALL_QUERY =
            "SELECT id, name FROM mparating WHERE id BETWEEN 1 AND 5 ORDER BY id";

    private static final String FIND_BY_ID_QUERY =
            "SELECT id, name FROM mparating WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<MpaRating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}