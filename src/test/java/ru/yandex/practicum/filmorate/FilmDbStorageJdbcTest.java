package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserRowMapper.class, UserDbStorage.class})
class FilmDbStorageJdbcTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    @Autowired
    FilmDbStorageJdbcTest(FilmDbStorage filmDbStorage, UserDbStorage userDbStorage, JdbcTemplate jdbc) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
        this.jdbc = jdbc;
    }

    private User newUser(String login) {
        User u = new User();
        u.setLogin(login);
        u.setEmail(login + "@mail.ru");
        u.setName(login);
        u.setBirthday(LocalDate.of(2000, 1, 1));
        return u;
    }

    private long addUser(String login) {
        return userDbStorage.add(newUser(login)).getId();
    }

    private Film newFilm(long mpaId, Set<Long> genreIds) {
        Film f = new Film();
        f.setName("Film-" + System.nanoTime());
        f.setDescription("desc");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(mpaId);
        f.setMpa(mpa);

        f.setGenres(
                genreIds.stream().map(id -> {
                    Genre g = new Genre();
                    g.setId(id);
                    return g;
                }).collect(java.util.stream.Collectors.toSet())
        );

        return f;
    }

    //Проверяет корректность сохранения MPA и жанров
    @Test
    void add_and_findById_shouldContainMpa_andGenres() {
        Film created = filmDbStorage.add(newFilm(1L, Set.of(1L, 2L)));

        Film found = filmDbStorage.findById(created.getId()).orElseThrow();

        assertThat(found.getMpa()).isNotNull();
        assertThat(found.getMpa().getId()).isEqualTo(1L);
        assertThat(found.getMpa().getName()).isNotNull();

        assertThat(found.getGenres()).isNotNull();
        assertThat(found.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(found.getGenres()).allSatisfy(g -> assertThat(g.getName()).isNotNull());
    }

    //Проверяет корректность удаления/пересоздания связей в film_genre при update() и обновление mparating_id
    @Test
    void update_shouldChangeMpa_andGenres() {
        Film created = filmDbStorage.add(newFilm(1L, Set.of(1L)));

        MpaRating mpa = new MpaRating();
        mpa.setId(2L);
        created.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(3L);
        created.setGenres(Set.of(genre));

        filmDbStorage.update(created);

        Film updated = filmDbStorage.findById(created.getId()).orElseThrow();
        assertThat(updated.getMpa().getId()).isEqualTo(2L);
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(3L);
    }

    //Проверяет, что лайки не дублируются
    @Test
    void addLike_shouldNotDuplicate() {
        long userId = addUser("u1");
        Film film = filmDbStorage.add(newFilm(1L, Set.of(1L)));

        filmDbStorage.addLike(film.getId(), userId);
        filmDbStorage.addLike(film.getId(), userId);

        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(), userId
        );

        assertThat(cnt).isEqualTo(1);
    }

    //проверяет корректность удаления лайков
    @Test
    void removeLike_shouldRemove() {
        long userId = addUser("u1");
        Film film = filmDbStorage.add(newFilm(1L, Set.of(1L)));

        filmDbStorage.addLike(film.getId(), userId);
        filmDbStorage.removeLike(film.getId(), userId);

        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(), userId
        );

        assertThat(cnt).isEqualTo(0);
    }

    //Проверяет правильный расчёт популярности и сортировку по лайкам
    @Test
    void getPopular_shouldSortByLikesDesc_thenId() {
        long u1 = addUser("u1");
        long u2 = addUser("u2");

        Film f1 = filmDbStorage.add(newFilm(1L, Set.of(1L))); // likes=1
        Film f2 = filmDbStorage.add(newFilm(1L, Set.of(1L))); // likes=2
        Film f3 = filmDbStorage.add(newFilm(1L, Set.of(1L))); // likes=0

        filmDbStorage.addLike(f1.getId(), u1);
        filmDbStorage.addLike(f2.getId(), u1);
        filmDbStorage.addLike(f2.getId(), u2);

        var popular = filmDbStorage.getPopular(10).stream().toList();
        assertThat(popular.get(0).getId()).isEqualTo(f2.getId());
        assertThat(popular.get(1).getId()).isEqualTo(f1.getId());
        assertThat(popular.stream().anyMatch(f -> f.getId().equals(f3.getId()))).isTrue();
    }
}