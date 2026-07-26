package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.validation.UserValidationMessages.*;

@Data
public class UpdateUserRequest {
    private Long id;
    private String login;
    private String email;
    private String name;

    @PastOrPresent(message = ERROR_BIRTHDAY_TOO_LATE)
    private LocalDate birthday;

    @Pattern(regexp = "^\\S+$", message = ERROR_LOGIN_CONTAINS_SPACES)
    private String loginForPattern;

    @Email(message = ERROR_INVALID_EMAIL)
    private String emailForValidation;

    @AssertTrue(message = ERROR_LOGIN_EMPTY)
    private boolean isLoginNotBlank() {
        return login == null || !login.isBlank();
    }

    @AssertTrue(message = ERROR_EMAIL_EMPTY)
    private boolean isEmailNotBlank() {
        return email == null || !email.isBlank();
    }

    public boolean hasLogin() {
        return login != null && !login.isBlank();
    }

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }
}