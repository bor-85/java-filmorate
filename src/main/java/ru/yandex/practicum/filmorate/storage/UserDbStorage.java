package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends StorageBaseOperations<User> implements UserStorage {

    private static final long STATUS_CONFIRMED_ID = 1L;

    private static final String FIND_ALL_QUERY =
            "SELECT id, name, login, email, birthday FROM users ORDER BY id";

    private static final String FIND_BY_ID_QUERY =
            "SELECT id, name, login, email, birthday FROM users WHERE id = ?";

    private static final String INSERT_USER_QUERY =
            "INSERT INTO users(login, email, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_USER_QUERY =
            "UPDATE users SET login = ?, email = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String ADD_FRIEND_QUERY =
            "INSERT INTO friendship(user_id, friend_id, status_id) " +
                    "SELECT ?, ?, ? " +
                    "WHERE NOT EXISTS ( " +
                    "  SELECT 1 FROM friendship WHERE user_id = ? AND friend_id = ? " +
                    ")";

    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String GET_FRIENDS_QUERY =
            "SELECT u.id, u.name, u.login, u.email, u.birthday " +
                    "FROM users u " +
                    "JOIN friendship f ON f.friend_id = u.id " +
                    "WHERE f.user_id = ? " +
                    "ORDER BY u.id";

    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT u.id, u.name, u.login, u.email, u.birthday " +
                    "FROM users u " +
                    "JOIN friendship f1 ON f1.friend_id = u.id " +
                    "JOIN friendship f2 ON f2.friend_id = u.id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ? " +
                    "ORDER BY u.id";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    @Transactional
    public User add(User user) {
        long id = insert(
                INSERT_USER_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                toTimestamp(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        update(
                UPDATE_USER_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                toTimestamp(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    @Transactional
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(
                ADD_FRIEND_QUERY,
                userId, friendId, STATUS_CONFIRMED_ID,
                userId, friendId
        );
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return findMany(GET_FRIENDS_QUERY, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        return findMany(GET_COMMON_FRIENDS_QUERY,
                userId, friendId
        );
    }

    private static Timestamp toTimestamp(java.time.LocalDate date) {
        return date == null ? null : Timestamp.valueOf(date.atStartOfDay());
    }
}