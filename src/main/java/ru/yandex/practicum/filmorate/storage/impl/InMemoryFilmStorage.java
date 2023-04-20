package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private HashMap<Integer, Film> films = new HashMap<>();
    private int idSequence = 0;

    @Override
    public Film create(Film film) {
        ValidationService.check(film);
        film.setId(++idSequence);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id = {} не найден", film.getId());
            throw new FilmNotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        ValidationService.check(film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(Film film) {
        films.remove(film.getId());
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film get(int id) {
        if (!films.containsKey(id)) {
            log.error("Фильм с id = {} не найден", id);
            throw new FilmNotFoundException("Фильм с id = " + id + " не найден");
        }
        return films.get(id);
    }

    @Override
    public Genre getGenre(int id) {
       throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public List<Genre> findAllGenres() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public Mpa getMpa(int id) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public List<Mpa> findAllMpa() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void likeFilm(Film film, User user) {
        film.getLikes().add(user.getId());
    }

    @Override
    public void dislikeFilm(Film film, User user) {
        film.getLikes().remove(user.getId());
    }

}
