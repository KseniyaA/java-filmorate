package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("InMemoryFilmStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void delete(Film film) {
        filmStorage.delete(film);
    }

    public Film get(int id) {
        return filmStorage.get(id);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    // добавление и удаление лайка
    public void addLike(Film film, User user) {
        film.getLikes().add(user.getId());
    }

    public void removeLike(Film film, User user) {
        film.getLikes().remove(user.getId());
    }

    // вывод 10 наиболее популярных фильмов по количеству лайков
    public List<Film> getPopularFilms(Optional<Integer> count) {
        List<Film> films = filmStorage.findAll();
        return films.stream().sorted(Comparator.comparingInt(f -> -f.getLikes().size())).limit(count.orElse(10))
                .collect(Collectors.toList());
    }
}
