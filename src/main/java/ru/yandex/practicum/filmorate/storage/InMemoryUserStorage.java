package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;


import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friends = new HashMap<>();
    private long nextId = 0;

    @Override
    public User add(User user) {
        long id = ++nextId;
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        users.replace(id, user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        Set<Long> friendsOfUser = friends.get(userId);
        if (friendsOfUser != null) {
            friendsOfUser.remove(friendId);
        }

        Set<Long> friendsOfFriend = friends.get(friendId);
        if (friendsOfFriend != null) {
            friendsOfFriend.remove(userId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        return friends.getOrDefault(userId, Set.of()).stream()
                .sorted()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        Set<Long> a = friends.getOrDefault(userId, Set.of());
        Set<Long> b = friends.getOrDefault(friendId, Set.of());

        return a.stream()
                .filter(b::contains)
                .sorted()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

}
