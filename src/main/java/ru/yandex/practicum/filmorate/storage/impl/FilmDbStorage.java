package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        ValidationService.check(film);

        String sqlQuery = "INSERT INTO film(name, release_date, description, duration, rating_id, rate) " +
                "values (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setDate(2, Date.valueOf(film.getReleaseDate()));
            stmt.setString(3, film.getDescription());
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            stmt.setInt(6, film.getRate());
            return stmt;
        }, keyHolder);

        int i = keyHolder.getKey().intValue();
        film.setId(i);

        if (!film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                String sqlQueryFilmGenre = "INSERT INTO film_genre(film_id, genre_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(sqlQueryFilmGenre, i, genre.getId());
            }
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        ValidationService.check(film);
        get(film.getId());
        String sqlQuery = "UPDATE film SET name = ?, release_date = ?, description = ?, duration = ?, rating_id = ?, rate = ?" +
                " WHERE id = ?";
        try {
            getMpa(film.getMpa().getId());
        } catch (MpaNotFoundException e) {
            throw new IllegalArgumentException("Переданный id рейтинг не существует");
        }

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                Date.valueOf(film.getReleaseDate()),
                film.getDescription(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getRate(),
                film.getId()
        );

        Set<Integer> genresIdsInDb = getAllGenresByFilmId(film.getId()).stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Integer> genresIdsInMemory = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());

        Set<Integer> commonElements = genresIdsInDb.stream()
                .distinct()
                .filter(genresIdsInMemory::contains)
                .collect(Collectors.toSet());

        Set<Integer> genresIdsForRemove = genresIdsInDb.stream().filter(g -> !commonElements.contains(g)).collect(Collectors.toSet());
        Set<Integer> genresIdsForInsert = genresIdsInMemory.stream().filter(g -> !commonElements.contains(g)).collect(Collectors.toSet());

        genresIdsForRemove.forEach(g -> this.deleteGenreFromFilm(film.getId(), g));
        genresIdsForInsert.forEach(g -> {
            try {
                getGenre(g);
                String sqlFilmGenre = "INSERT INTO film_genre(film_id, genre_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(sqlFilmGenre, film.getId(), g);
            } catch (GenreNotFoundException e) {
                log.debug("Жанр с id = {} не существует", g);
            }
        });

        return get(film.getId());
    }

    @Override
    public void delete(Film film) {
        String sqlQuery = "DELETE FROM film WHERE id = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT  id, name, release_date, description, duration, rate, rating_id FROM film ";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film get(int id) {
        try {
            String sqlQuery = "select id, name, release_date, description, duration, rate, rating_id " +
                    "from film where id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильм с id = {} не найден", id);
            throw new FilmNotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    private Film mapRowToFilm(ResultSet rs, int i) throws SQLException {
        Mpa mpa = getMpa(rs.getInt("rating_id"));
        List<Genre> genres = getAllGenresByFilmId(rs.getInt("id"));

        Film film = Film.builder().id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .rate(rs.getInt("rate"))
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .genres(new HashSet<>(genres))
                .likes(new HashSet<>(getLikes(rs.getInt("id"))))
                .build();
        return film;
    }

    @Override
    public Genre getGenre(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM genre WHERE id = ?", this::makeGenre, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Жанр с id = {} не найден", id);
            throw new GenreNotFoundException("Жанр с id = " + id + " не найден");
        }
    }

    @Override
    public List<Genre> findAllGenres() {
        String sql = "SELECT * FROM genre";
        return jdbcTemplate.query(sql, this::makeGenre);
    }

    public List<Genre> getAllGenresByFilmId(Integer filmId) {
        String sql = "SELECT g.* FROM film_genre AS fg " +
                "INNER JOIN genre AS g  ON g.id = fg.genre_id " +
                "WHERE film_id = ?";
        return jdbcTemplate.query(sql, this::makeGenre, filmId);
    }

    public List<Integer> getLikes(Integer filmId) {
        String sql = "SELECT * FROM favorite_films " +
                "WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeLikes(rs), filmId);
    }

    private Integer makeLikes(ResultSet rs) throws SQLException {
        return rs.getInt("user_id");
    }

    public void deleteGenreFromFilm(int filmId, int genreId) {
        String sqlQuery = "DELETE FROM film_genre WHERE film_id = ? and genre_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, genreId);
    }

    @Override
    public Mpa getMpa(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM rating WHERE id = ?", (rs, rowNum) -> makeRating(rs), id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Рейтинг с id = {} не найден", id);
            throw new MpaNotFoundException("Рейтинг с id = " + id + " не найден");
        }
    }

    @Override
    public List<Mpa> findAllMpa() {
        String sql = "SELECT * FROM rating";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeRating(rs));
    }

    @Override
    public void likeFilm(Film film, User user) {
        String sqlFilmGenre = "INSERT INTO favorite_films(film_id, user_id) " +
                    "values (?, ?)";
        jdbcTemplate.update(sqlFilmGenre, film.getId(), user.getId());
    }

    @Override
    public void dislikeFilm(Film film, User user) {
        String sql = "DELETE FROM favorite_films " +
                "WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, film.getId(), user.getId());
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(
                rs.getInt("id"),
                rs.getString("name")
        );
    }

    private Mpa makeRating(ResultSet rs) throws SQLException {
        return new Mpa(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
