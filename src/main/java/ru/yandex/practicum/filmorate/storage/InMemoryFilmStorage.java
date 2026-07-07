package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.*;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long nextId = 0;

    @Override
    public Film add(Film film) {
        long id = ++nextId;
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        Film oldFilm = films.get(id);
        if (oldFilm == null) {
            throw new NotFoundException(ERROR_ID_NOT_FOUND + id);
        }
        setFilmFields(oldFilm, film);

        return oldFilm;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public void addLike(Long film_id, Long user_id) {
        likes.computeIfAbsent(film_id, k -> new HashSet<>()).add(user_id);
    }

    @Override
    public void removeLike(Long film_id, Long user_id) {
        Set<Long> filmLikes = likes.get(film_id);
        if (filmLikes != null) {
            filmLikes.remove(user_id);
        }
    }

    @Override
    public int getLikeCount(Long film_id) {
        return likes.getOrDefault(film_id, Set.of()).size();
    }

    private void setFilmFields(Film oldFilm, Film newFilm) {
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setName(newFilm.getName());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
    }
}
