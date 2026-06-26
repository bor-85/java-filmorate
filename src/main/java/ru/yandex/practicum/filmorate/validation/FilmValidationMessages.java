package ru.yandex.practicum.filmorate.validation;

public class FilmValidationMessages {
    public static final String ERROR_NAME_EMPTY = "Название не может быть пустым";
    public static final String ERROR_DESCRIPTION_TOO_LONG = "Описание не может быть длиннее 200 символов";
    public static final String ERROR_RELEASEDATE_IS_NULL = "Дата релиза должна быть указана";
    public static final String ERROR_RELEASEDATE_TOO_EARLY = "Дата релиза не может быть раньше 28 декабря 1895 года";
    public static final String ERROR_INVALID_DURATION = "Продолжительность должна быть положительной";
}
