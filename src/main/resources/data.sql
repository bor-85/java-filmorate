--Заполнение справочника mparating
INSERT INTO mparating (id, name)
SELECT 1, 'G'
    WHERE NOT EXISTS (SELECT 1 FROM mparating WHERE id = 1);

INSERT INTO mparating (id, name)
SELECT 2, 'PG'
    WHERE NOT EXISTS (SELECT 1 FROM mparating WHERE id = 2);

INSERT INTO mparating (id, name)
SELECT 3, 'PG-13'
    WHERE NOT EXISTS (SELECT 1 FROM mparating WHERE id = 3);

INSERT INTO mparating (id, name)
SELECT 4, 'R'
    WHERE NOT EXISTS (SELECT 1 FROM mparating WHERE id = 4);

INSERT INTO mparating (id, name)
SELECT 5, 'NC-17'
    WHERE NOT EXISTS (SELECT 1 FROM mparating WHERE id = 5);

--Заполнение справочника жанров
INSERT INTO genres (id, name)
SELECT 1, 'Комедия'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 1);

INSERT INTO genres (id, name)
SELECT 2, 'Драма'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 2);

INSERT INTO genres (id, name)
SELECT 3, 'Мультфильм'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 3);

INSERT INTO genres (id, name)
SELECT 4, 'Боевик'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 4);

INSERT INTO genres (id, name)
SELECT 5, 'Триллер'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 5);

INSERT INTO genres (id, name)
SELECT 6, 'Документальный'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 6);

--статусы дружбы
INSERT INTO friendship_status (id, name)
SELECT 1, 'CONFIRMED'
    WHERE NOT EXISTS (SELECT 1 FROM friendship_status WHERE id = 1);

INSERT INTO friendship_status (id, name)
SELECT 2, 'NOT_CONFIRMED'
    WHERE NOT EXISTS (SELECT 1 FROM friendship_status WHERE id = 2);

--чистка перед каждым запуском
DELETE FROM likes;

DELETE FROM film_genre;

DELETE FROM friendship;

DELETE FROM films;

DELETE FROM users;