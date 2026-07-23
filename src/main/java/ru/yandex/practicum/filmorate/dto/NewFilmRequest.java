package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

import static ru.yandex.practicum.filmorate.model.Film.MAX_LENGTH_DESCRIPTION;
import static ru.yandex.practicum.filmorate.model.Film.MIN_RELEASE_DATE;
import static ru.yandex.practicum.filmorate.validation.FilmValidationMessages.*;

@Data
public class NewFilmRequest {

    @NotBlank(message = ERROR_NAME_EMPTY)
    private String name;

    @Size(max = MAX_LENGTH_DESCRIPTION, message = ERROR_DESCRIPTION_TOO_LONG)
    private String description;

    @NotNull(message = ERROR_RELEASEDATE_IS_NULL)
    private LocalDate releaseDate;

    @AssertTrue(message = ERROR_RELEASEDATE_TOO_EARLY)
    private boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(MIN_RELEASE_DATE);
    }

    @Positive(message = ERROR_INVALID_DURATION)
    private int duration;

    private MpaaRatingDto mpa;

    private Set<GenreDto> genres;
}