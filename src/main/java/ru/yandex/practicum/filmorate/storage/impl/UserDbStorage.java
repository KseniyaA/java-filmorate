package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Slf4j
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        if (Optional.ofNullable(user.getName()).isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        ValidationService.check(user);

        String sqlQuery = "INSERT INTO users(email, login, name, birthday) " +
                "values (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        int i = keyHolder.getKey().intValue();
        user.setId(i);
        return user;
    }

    @Override
    public User update(User user) {
        get(user.getId());
        if (Optional.ofNullable(user.getName()).isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        ValidationService.check(user);

        String sqlQuery = "update users set " +
                "email = ?, login = ?, name = ? , birthday = ? " +
                "where id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return get(user.getId());
    }

    @Override
    public void delete(User user) {
        String sqlQuery = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sqlQuery, user.getId());
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> userList = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
        for (User u : userList) {
            List<Integer> friendsByUser = findFriendsIdsByUser(u);
            u.getFriends().addAll(new HashSet<>(friendsByUser));
        }
        return userList;
    }

    @Override
    public User get(int id) {
        User user;
        try {
            user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", (rs, rowNum) -> makeUser(rs), id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id = {} не найден", id);
            throw new UserNotFoundException("Пользователь с id = " + id + " не найден");
        }
        List<Integer> friendsByUser = findFriendsIdsByUser(user);
        user.getFriends().addAll(new HashSet<>(friendsByUser));
        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        get(friendId);
        String sqlQuery = "INSERT INTO friendship(user_from, user_to, is_confirmed) " +
                "values (?, ?, ?)";

        jdbcTemplate.update(sqlQuery,
                userId,
                friendId,
                false);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sqlQuery = "DELETE FROM friendship WHERE user_from = ? and user_to = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT DISTINCT u.* " +
                "                FROM FRIENDSHIP f1 " +
                "        INNER JOIN FRIENDSHIP f2 ON f1.user_to = f2.user_to " +
                "        INNER JOIN users u ON u.id = f1.user_to " +
                "        WHERE f1.user_from = ? " +
                "        AND f2.user_from = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId, otherId);
    }

    @Override
    public List<User> getUserFriends(int userId) {
        String sql = "SELECT DISTINCT u.* " +
                "FROM FRIENDSHIP f " +
                "INNER JOIN users u ON u.id = f.user_to " +
                "WHERE f.user_from = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId);
    }

    private List<Integer> findFriendsIdsByUser(User user) {
        String sql = "SELECT * FROM friendship WHERE user_from = ? and is_confirmed = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFriend(rs), user.getId(), true);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate(),
                new HashSet<>()
        );
    }

    private Integer makeFriend(ResultSet rs) throws SQLException {
        return rs.getInt("user_to");
    }
}
