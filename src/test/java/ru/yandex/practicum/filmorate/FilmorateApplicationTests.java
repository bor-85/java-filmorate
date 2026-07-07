package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.validation.FilmValidationMessages.*;
import static ru.yandex.practicum.filmorate.validation.UserValidationMessages.*;

@SpringBootTest
class FilmorateApplicationTests {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private <T> Set<ConstraintViolation<T>> validate(T obj) {
		return validator.validate(obj);
	}

	private final InMemoryUserStorage userStorage = new InMemoryUserStorage();
	private final InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
	private final UserService userService = new UserService(userStorage);
	private final FilmService filmService = new FilmService(filmStorage, userStorage);

	private User createUser(String login) {
		User u = new User();
		u.setLogin(login);
		u.setEmail(login + "@mail.ru");
		u.setName("");
		u.setBirthday(LocalDate.of(2000, 1, 1));
		return userService.add(u);
	}

	private Film createFilm(String name) {
		Film f = new Film();
		f.setName(name);
		f.setDescription("desc");
		f.setReleaseDate(LocalDate.of(2000, 1, 1));
		f.setDuration(10);
		return filmService.add(f);
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

	// Тесты UserService

	//Проверка, что произошло взаимное добавление в друзья
	@Test
	void addFriend_shouldBeMutual() {
		User a = createUser("a");
		User b = createUser("b");

		userService.addFriend(a.getId(), b.getId());

		var aFriends = userService.getFriends(a.getId());
		var bFriends = userService.getFriends(b.getId());

		assertEquals(1, aFriends.size());
		assertEquals(b.getId(), aFriends.get(0).getId());

		assertEquals(1, bFriends.size());
		assertEquals(a.getId(), bFriends.get(0).getId());
	}

	//Удаление из друзей должно быть взаимным
	@Test
	void removeFriend_shouldBeMutual() {
		User a = createUser("a");
		User b = createUser("b");

		userService.addFriend(a.getId(), b.getId());
		assertDoesNotThrow(() -> userService.removeFriend(a.getId(), b.getId()));

		assertTrue(userService.getFriends(a.getId()).isEmpty());
		assertTrue(userService.getFriends(b.getId()).isEmpty());
	}

	// Функция возврата списка общих друзей должна выдавать пересечение по друзьям
	@Test
	void getCommonFriends_shouldReturnIntersection() {
		User u1 = createUser("u1");
		User u2 = createUser("u2");
		User u3 = createUser("u3");
		User u4 = createUser("u4");

		// u1 -> u2, u3
		userService.addFriend(u1.getId(), u2.getId());
		userService.addFriend(u1.getId(), u3.getId());

		// u4 -> u2 (общий)
		userService.addFriend(u4.getId(), u2.getId());
		userService.addFriend(u4.getId(), createUser("uX").getId());

		var common = userService.getCommonFriends(u1.getId(), u4.getId())
				.stream()
				.map(User::getId)
				.toList();

		assertEquals(1, common.size());
		assertEquals(u2.getId(), common.get(0));
	}

	// Тесты FilmService

	//При добавлении лайка 2 раза он не должен дублироваться
	@Test
	void addLike_shouldNotDuplicate() {
		Film film = createFilm("film");
		User user = createUser("u1");

		filmService.addLike(film.getId(), user.getId());
		filmService.addLike(film.getId(), user.getId());

		assertEquals(1, filmStorage.getLikeCount(film.getId()));
	}

	// Удаление несуществующего лайка не должно приводить к ошибке
	@Test
	void removeLike_whenUserDidntLike_shouldNotThrow() {
		Film film = createFilm("film");
		User user = createUser("u1");

		assertDoesNotThrow(() -> filmService.removeLike(film.getId(), user.getId()));
		assertEquals(0, filmStorage.getLikeCount(film.getId()));
	}

	// Удаление лайка происходит
	@Test
	void removeLike_whenUserHasLiked_shouldRemoveLike() {
		Film film = createFilm("film");
		User user = createUser("u1");

		// ставим лайк
		filmService.addLike(film.getId(), user.getId());
		assertEquals(1, filmStorage.getLikeCount(film.getId()));

		// удаляем лайк
		assertDoesNotThrow(() -> filmService.removeLike(film.getId(), user.getId()));

		// проверяем, что лайков больше нет
		assertEquals(0, filmStorage.getLikeCount(film.getId()));
	}

	//проверка порядка сортировки топ-10 по убыванию лайков
	@Test
	void getPopular_shouldSortByLikeCountDesc() {
		User u1 = createUser("u1");
		User u2 = createUser("u2");

		Film f1 = createFilm("f1"); // id=1
		Film f2 = createFilm("f2"); // id=2
		Film f3 = createFilm("f3"); // id=3

		filmService.addLike(f1.getId(), u1.getId());
		filmService.addLike(f2.getId(), u1.getId());
		filmService.addLike(f2.getId(), u2.getId());

		var popularIds = filmService.getPopular(10).stream()
				.map(Film::getId)
				.toList();

		assertEquals(List.of(f2.getId(), f1.getId(), f3.getId()), popularIds);
	}

}
