package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component("filmRowMapperWithoutGenres")
public class FilmRowMapperWithoutGenres implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("film_description"));

        var releaseDate = rs.getDate("film_release_date").toLocalDate();
        film.setReleaseDate(releaseDate);

        film.setDuration(rs.getInt("film_duration"));

        MpaRating mpa = new MpaRating();
        Long mpaId = rs.getObject("mpa_id", Long.class);
        if (mpaId != null) {
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
        }
        film.setMpa(mpa);

        film.setGenres(java.util.Set.of());

        return film;
    }
}