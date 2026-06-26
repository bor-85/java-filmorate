package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static ru.yandex.practicum.filmorate.validation.FilmValidationMessages.*;
import static ru.yandex.practicum.filmorate.validation.UserValidationMessages.*;

@SpringBootTest
class FilmorateApplicationTests {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private <T> Set<ConstraintViolation<T>> validate(T obj) {
		return validator.validate(obj);
	}

	//Тесты класса User
	//Проверка валидации на пустой логин
	@Test
	void whenLoginBlankShouldBeMessage() {
		User user = new User();
		user.setLogin("");
		user.setEmail("mail@mail.ru");

		Set<ConstraintViolation<User>> violations = validate(user);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_LOGIN_EMPTY)));
	}

	//Проверка валидации на содержание пробелов в логине
	@Test
	void whenLoginContainsSpacesShouldBeMessage() {
		User user = new User();
		user.setLogin("dolore test");
		user.setEmail("mail@mail.ru");

		Set<ConstraintViolation<User>> violations = validate(user);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_LOGIN_CONTAINS_SPACES)));
	}

	//Проверка валидации, что mail содержит @
	@Test
	void whenEmailInvalidShouldBeMessage() {
		User user = new User();
		user.setLogin("dolore");
		user.setEmail("invalid-email");

		Set<ConstraintViolation<User>> violations = validate(user);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_INVALID_EMAIL)));
	}

	//Проверка валидации, что mail не null
	@Test
	void whenEmailEmptyShouldBeMessage() {
		User user = new User();
		user.setLogin("dolore");

		Set<ConstraintViolation<User>> violations = validate(user);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_EMAIL_EMPTY)));
	}

	//Проверка валидации, что дата рождения не больше текущей даты
	@Test
	void whenBirthdayInFutureShouldBeMessage() {
		User user = new User();
		user.setLogin("dolore");
		user.setEmail("mail@mail.ru");
		user.setBirthday(LocalDate.now().plusDays(1));

		Set<ConstraintViolation<User>> violations = validate(user);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_BIRTHDAY_TOO_LATE)));
	}

	//Проверка валидации, что с корректными данными пользователь создается
	@Test
	void whenValidUserShouldNoMessage() {
		User user = new User();
		user.setLogin("dolore");
		user.setEmail("mail@mail.ru");
		user.setName("Nick");
		user.setBirthday(LocalDate.of(2000, 1, 1));

		Set<ConstraintViolation<User>> violations = validate(user);
		assertTrue(violations.isEmpty());
	}

	//Тесты класса Film

	//Проверка валидации, что название фильма не пустое
	@Test
	void whenNameBlankShouldBeMessage() {
		Film film = new Film();
		film.setName("");
		film.setDescription("desc");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validate(film);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_NAME_EMPTY)));
	}

	//Проверка валидации, что длина строки описания фильма не более 200 символов
	@Test
	void whenDescriptionTooLongShouldBeMessage() {
		Film film = new Film();
		film.setName("Test");
		film.setDescription("a".repeat(201)); // > 200
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validate(film);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_DESCRIPTION_TOO_LONG)));
	}

	//Проверка валидации, что длительность фильма - положительное число
	@Test
	void whenDurationNotPositiveShouldBeMessage() {
		Film film = new Film();
		film.setName("Test");
		film.setDescription("desc");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(0);

		Set<ConstraintViolation<Film>> violations = validate(film);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_INVALID_DURATION)));
	}

	//Проверка валидации, что дата выпуска фильма не ранее 1895 года
	@Test
	void whenReleaseDateTooEarlyShouldBeMessage() {
		Film film = new Film();
		film.setName("Test");
		film.setDescription("desc");
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validate(film);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_RELEASEDATE_TOO_EARLY)));
	}

	//Проверка валидации, что дата выпуска фильма не может быть пустой
	@Test
	void whenReleaseDateNullShouldBeMessage() {
		Film film = new Film();
		film.setName("Test");
		film.setDescription("desc");
		film.setReleaseDate(null);
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validate(film);

		assertTrue(violations.stream().anyMatch(v ->
				v.getMessage().equals(ERROR_RELEASEDATE_IS_NULL)));
	}

	//Если данные фильма валидные, то ошибок быть не должно
	@Test
	void whenValidFilmShouldNoMessage() {
		Film film = new Film();
		film.setName("Test");
		film.setDescription("desc");
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		film.setDuration(1);

		Set<ConstraintViolation<Film>> violations = validate(film);
		assertTrue(violations.isEmpty());
	}

}
