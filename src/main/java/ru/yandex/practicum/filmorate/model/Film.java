package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    public static final String ERROR_NAME_EMPTY = "Название не может быть пустым";
    public static final String ERROR_DESCRIPTION_TOO_LONG = "Описание не может быть длиннее 200 символов";
    public static final String ERROR_RELEASEDATE_IS_NULL = "Дата релиза должна быть указана";
    public static final String ERROR_RELEASEDATE_TOO_EARLY = "Дата релиза не может быть раньше 28 декабря 1895 года";
    public static final String ERROR_INVALID_DURATION = "Продолжительность должна быть положительной";
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_LENGTH_DESCRIPTION = 200;

    Long id;

    @NotBlank(message = ERROR_NAME_EMPTY)
    String name;

    @Size(max = MAX_LENGTH_DESCRIPTION, message = ERROR_DESCRIPTION_TOO_LONG)
    String description;

    @NotNull(message = ERROR_RELEASEDATE_IS_NULL)
    LocalDate releaseDate;

    @AssertTrue(message = ERROR_RELEASEDATE_TOO_EARLY)
    boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(MIN_RELEASE_DATE);
    }

    @Positive(message = ERROR_INVALID_DURATION)
    int duration;
}
