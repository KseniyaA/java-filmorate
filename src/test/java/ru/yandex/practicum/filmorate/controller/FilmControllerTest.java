package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private final FilmService filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage());
    private final FilmController filmController = new FilmController(filmService);

    @Test
    void create() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();
        Film createdFilm = filmController.create(film);
        assertTrue(createdFilm.getId() > 0);
        assertEquals("name", createdFilm.getName());
        assertEquals("description", createdFilm.getDescription());
        assertEquals(100, createdFilm.getDuration());
        assertEquals(LocalDate.of(2020, 1, 1), createdFilm.getReleaseDate());
    }

    @Test
    void createWithNullName() {
        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> {
                    Film film = Film.builder()
                            .rate(1)
                            .description("description")
                            .duration(100)
                            .releaseDate(LocalDate.of(2020, 1, 1))
                            .build();
                });
    }

    @Test
    void createWithEmptyName() {
        Film film = Film.builder()
                .name("")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    filmController.create(film);
                });
    }

    @Test
    void createWithDescription200Name() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("*".repeat(200))
                .duration(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm);

    }

    @Test
    void createWithDescriptionMore200Name() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("*".repeat(201))
                .duration(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    filmController.create(film);
                });
    }

    @Test
    void createWithIncorrectReleaseDate() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(1895, 12, 28).minusDays(1))
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    filmController.create(film);
                });
    }

    @Test
    void createWithCorrectReleaseDate() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(1895, 12, 28))
                .build();
        filmController.create(film);
    }

    @Test
    void createWithCorrectDuration0() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(0)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    filmController.create(film);
                });
    }

    @Test
    void createWithCorrectDurationNegative() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(-100)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    filmController.create(film);
                });
    }

    @Test
    void update() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();
        filmController.create(film);

        Film updateFilm = Film.builder()
                .id(film.getId())
                .name("name1")
                .rate(2)
                .description("description1")
                .duration(100)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();
        Film updatedFilm = filmController.update(updateFilm);

        assertEquals(1, filmController.findAll().size());
        assertEquals("name1", updatedFilm.getName());
        assertEquals(2, updatedFilm.getRate());
        assertEquals("description1", updatedFilm.getDescription());
        assertEquals(100, updatedFilm.getDuration());
        assertEquals(LocalDate.of(2000, 12, 28), updatedFilm.getReleaseDate());
    }

    @Test
    void updateUnExistedFilm() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();
        filmController.create(film);

        Film updateFilm = Film.builder()
                .id(999)
                .name("name1")
                .rate(2)
                .description("description1")
                .duration(100)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();

        final FilmNotFoundException exception = assertThrows(
                FilmNotFoundException.class,
                () -> {
                    filmController.update(updateFilm);
                });
        assertEquals("Фильм с id = 999 не найден", exception.getMessage());
    }

    @Test
    public void getAll() {
        Film film = Film.builder()
                .name("name")
                .rate(1)
                .description("description")
                .duration(100)
                .releaseDate(LocalDate.of(2000, 12, 28))
                .build();
        filmController.create(film);
        Film film2 = Film.builder()
                .name("name2")
                .rate(2)
                .description("description2")
                .duration(200)
                .releaseDate(LocalDate.of(2002, 12, 28))
                .build();
        filmController.create(film2);

        assertEquals(2, filmController.findAll().size());
    }

    @Test
    public void createWithNullBody() {
        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> {
                    filmController.create(null);
                });

    }
}