package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@Slf4j
public class MpaController {
    private final FilmService filmService;

    @Autowired
    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/mpa/{id}")
    public Mpa getRating(@PathVariable int id) {
        log.info("Получен запрос GET /mpa/{id} с параметрами id = {}", id);
        return filmService.getMpa(id);
    }

    @GetMapping("/mpa")
    public List<Mpa> findAllGRatings() {
        log.info("Получен запрос GET /mpa.");
        return filmService.findAllMpa();
    }
}
