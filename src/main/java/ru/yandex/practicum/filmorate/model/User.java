package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;
import static ru.yandex.practicum.filmorate.validation.UserValidationMessages.*;

@Data
public class User {

    private Long id;

    @NotBlank(message = ERROR_LOGIN_EMPTY)
    @Pattern(regexp = "^\\S+$", message = ERROR_LOGIN_CONTAINS_SPACES)
    private String login;

    @NotBlank(message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_INVALID_EMAIL)
    private String email;

    private String name;

    @PastOrPresent(message = ERROR_BIRTHDAY_TOO_LATE)
    private LocalDate birthday;
}
