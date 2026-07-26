package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

import static ru.yandex.practicum.filmorate.model.Film.MAX_LENGTH_DESCRIPTION;
import static ru.yandex.practicum.filmorate.model.Film.MIN_RELEASE_DATE;
import static ru.yandex.practicum.filmorate.validation.FilmValidationMessages.*;

@Data
public class UpdateFilmRequest {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    private MpaaRatingDto mpa;
    private Set<GenreDto> genres;

    @AssertTrue(message = ERROR_NAME_EMPTY)
    private boolean isNameValid() {
        return name == null || !name.isBlank();
    }

    @AssertTrue(message = ERROR_RELEASEDATE_TOO_EARLY)
    private boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(MIN_RELEASE_DATE);
    }

    @Size(max = MAX_LENGTH_DESCRIPTION, message = ERROR_DESCRIPTION_TOO_LONG)
    public String getDescription() {
        return description;
    }

    @Positive(message = ERROR_INVALID_DURATION)
    public Integer getDuration() {
        return duration;
    }

    public boolean hasMpa() {
        return mpa != null && mpa.getId() != null;
    }

    public boolean hasGenres() {
        return genres != null;
    }
}