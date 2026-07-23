package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageJdbcTest {

    private final UserDbStorage userDbStorage;

    @Autowired
    UserDbStorageJdbcTest(UserDbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    private User newUser(String login) {
        User u = new User();
        u.setLogin(login);
        u.setEmail(login + "@mail.ru");
        u.setName(login);
        u.setBirthday(LocalDate.of(2000, 1, 1));
        return u;
    }

    // Проверяет: добавление пользователя в БД и корректное чтение его по id через findById
    @Test
    void add_and_findById_shouldReturnUser() {
        User created = userDbStorage.add(newUser("u1"));

        var found = userDbStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("u1");
    }

    // Проверяет: обновление полей пользователя (name/email) через update и их сохранение в БД
    @Test
    void update_shouldChangeFields() {
        User u = userDbStorage.add(newUser("u1"));
        u.setName("NewName");
        u.setEmail("new@mail.ru");

        userDbStorage.update(u);

        User updated = userDbStorage.findById(u.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");
    }

    // Проверяет: метод findAll возвращает всех пользователей, которые были добавлены в БД
    @Test
    void findAll_shouldReturnAllUsers() {
        User u1 = userDbStorage.add(newUser("u1"));
        User u2 = userDbStorage.add(newUser("u2"));

        var all = userDbStorage.findAll();
        assertThat(all).extracting(User::getId).containsExactlyInAnyOrder(u1.getId(), u2.getId());
    }

    // Проверяет: одностороннюю дружбу
    @Test
    void addFriend_isOneWay_and_getFriendsReflectsOutgoingRelation() {
        User a = userDbStorage.add(newUser("a"));
        User b = userDbStorage.add(newUser("b"));

        userDbStorage.addFriend(a.getId(), b.getId());

        assertThat(userDbStorage.getFriends(a.getId()))
                .extracting(User::getId)
                .containsExactly(b.getId());

        assertThat(userDbStorage.getFriends(b.getId()))
                .extracting(User::getId)
                .doesNotContain(a.getId());
    }

    // Проверяет удаление дружбы
    @Test
    void removeFriend_shouldDeleteRelationship() {
        User a = userDbStorage.add(newUser("a"));
        User b = userDbStorage.add(newUser("b"));

        userDbStorage.addFriend(a.getId(), b.getId());
        userDbStorage.removeFriend(a.getId(), b.getId());

        assertThat(userDbStorage.getFriends(a.getId()))
                .extracting(User::getId)
                .isEmpty();
    }

    // Проверяет: getCommonFriends возвращает пересечение outgoing-друзей двух пользователей.
    @Test
    void getCommonFriends_shouldReturnIntersectionOfOutgoingFriends() {
        User u1 = userDbStorage.add(newUser("u1"));
        User u2 = userDbStorage.add(newUser("u2"));
        User common = userDbStorage.add(newUser("common"));

        userDbStorage.addFriend(u1.getId(), common.getId());
        userDbStorage.addFriend(u2.getId(), common.getId());

        var commonFriends = userDbStorage.getCommonFriends(u1.getId(), u2.getId());
        assertThat(commonFriends).extracting(User::getId).containsExactly(common.getId());
    }
}