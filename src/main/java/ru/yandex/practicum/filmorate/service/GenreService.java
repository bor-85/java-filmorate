package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validation.FilmHandleMessages.ERROR_ID_NOT_FOUND_GENRE;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public List<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre findById(Long id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_ID_NOT_FOUND_GENRE + id));
    }
}