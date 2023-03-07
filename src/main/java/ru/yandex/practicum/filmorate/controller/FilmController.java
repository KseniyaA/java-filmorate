package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
public class FilmController {
    private HashMap<Integer, Film> films = new HashMap<>();
    private int idSequence = 0;

    @GetMapping("/films")
    public List<Film> findAll() {
        log.info("Получен запрос GET /films.");
        return new ArrayList<>(films.values());
    }

    @PostMapping("/films")
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос POST /films с параметрами {}", film);
        film.setId(++idSequence);
        check(film);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping("/films")
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос PUT /films с параметрами {}", film);
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id = {} не найден", film.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм с id = " + film.getId() + " не найден");
        }
        check(film);
        films.put(film.getId(), film);
        return film;
    }

    public HashMap<Integer, Film> getFilms() {
        return films;
    }

    private void check(Film film) {
        if (!StringUtils.hasLength(film.getName())) {
            log.warn("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Максимальная длина описания - 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Продолжительность фильма должна быть положительной");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
