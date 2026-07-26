package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("film_description"));

        LocalDate releaseDate = rs.getDate("film_release_date").toLocalDate();
        film.setReleaseDate(releaseDate);

        film.setDuration(rs.getInt("film_duration"));

        MpaRating mpa = new MpaRating();
        Long mpaId = rs.getObject("mpa_id", Long.class);
        if (mpaId != null) {
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
        }
        film.setMpa(mpaId == null ? null : mpa);

        Long genreId = rs.getObject("genre_id", Long.class);
        if (genreId != null) {
            Genre genre = new Genre();
            genre.setId(genreId);
            genre.setName(rs.getString("genre_name"));

            HashSet<Genre> genres = new HashSet<>();
            genres.add(genre);
            film.setGenres(genres);
        } else {
            film.setGenres(new HashSet<>());
        }

        return film;
    }
}
