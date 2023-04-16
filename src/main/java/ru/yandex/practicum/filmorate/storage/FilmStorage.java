package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    void delete(Film film);

    List<Film> findAll();

    Film get(int id);

    Genre getGenre(int id);

    List<Genre> findAllGenres();

    Mpa getMpa(int id);

    List<Mpa> findAllMpa();

    void likeFilm(Film film, User user);

    void dislikeFilm(Film film, User user);
}
