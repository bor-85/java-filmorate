package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.validation.FilmValidationMessages.*;

@Data
public class Film {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_LENGTH_DESCRIPTION = 200;

    Long id;

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

}
