package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class User {
    public static final String ERROR_LOGIN_EMPTY = "Логин не может быть пустым";
    public static final String ERROR_LOGIN_CONTAINS_SPACES = "Логин не может содержать пробелы";
    public static final String ERROR_EMAIL_EMPTY = "Email не должен быть пустым";
    public static final String ERROR_INVALID_EMAIL = "Email должен содержать символ '@'";
    public static final String ERROR_BIRTHDAY_TOO_LATE = "Дата рождения не может быть больше текущей даты";

    Long id;

    @NotBlank(message = ERROR_LOGIN_EMPTY)
    @Pattern(regexp = "^\\S+$", message = ERROR_LOGIN_CONTAINS_SPACES)
    String login;

    @NotBlank(message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_INVALID_EMAIL)
    String email;

    String name;

    @PastOrPresent(message = ERROR_BIRTHDAY_TOO_LATE)
    LocalDate birthday;
}
