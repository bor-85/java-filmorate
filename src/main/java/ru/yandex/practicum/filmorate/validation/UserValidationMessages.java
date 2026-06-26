package ru.yandex.practicum.filmorate.validation;

public class UserValidationMessages {
    public static final String ERROR_LOGIN_EMPTY = "Логин не может быть пустым";
    public static final String ERROR_LOGIN_CONTAINS_SPACES = "Логин не может содержать пробелы";
    public static final String ERROR_EMAIL_EMPTY = "Email не должен быть пустым";
    public static final String ERROR_INVALID_EMAIL = "Email должен содержать символ '@'";
    public static final String ERROR_BIRTHDAY_TOO_LATE = "Дата рождения не может быть больше текущей даты";
}
