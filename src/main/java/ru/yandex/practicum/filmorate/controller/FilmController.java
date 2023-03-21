package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@RestController
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping("/films")
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос POST /films с параметрами {}", film);
        return filmService.create(film);
    }

    @PutMapping("/films")
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос PUT /films с параметрами {}", film);
        return filmService.update(film);
    }

    @DeleteMapping("/films")
    public void delete(@RequestBody Film film) {
        log.info("Получен запрос DELETE /films с параметрами {}", film);
        filmService.delete(film);
    }

    @GetMapping("films/{id}")
    public Film get(@PathVariable int id) {
        log.info("Получен запрос GET /films/{id} с параметрами {}", id);
        return filmService.get(id);
    }

    @GetMapping("/films")
    public List<Film> findAll() {
        log.info("Получен запрос GET /films.");
        return filmService.findAll();
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void likeFilm(@PathVariable("id") int filmId, @PathVariable int userId) {
        log.info("Получен запрос PUT /films/{id}/like/{userId} с параметрами filmId = {}, userId = {}", filmId, userId);
        filmService.likeFilm(filmId, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void dislikeFilm(@PathVariable("id") int filmId, @PathVariable int userId) {
        log.info("Получен запрос DELETE /films/{id}/like/{userId} с параметрами id = {}, userId = {}", filmId, userId);
        filmService.dislikeFilm(filmId, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count) {
        log.info("Получен запрос GET /films/popular?count={count} с параметрами count = {}", count);
        return filmService.getPopularFilms(count);
    }
}
