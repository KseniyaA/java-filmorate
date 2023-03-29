package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private HashMap<Integer, User> users = new HashMap<>();
    private int idSequence = 0;

    @Override
    public User create(User user) {
        check(user);
        user.setId(++idSequence);
        if (Optional.ofNullable(user.getName()).isEmpty() || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id = {} не найден", user.getId());
            throw new UserNotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        check(user);
        if (Optional.ofNullable(user.getName()).isEmpty()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(User user) {
        users.remove(user.getId());
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User get(int id) {
        if (!users.containsKey(id)) {
            log.error("Пользователь с id = {} не найден", id);
            throw new UserNotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    private void check(User user) {
        if (!StringUtils.hasLength(user.getEmail())) {
            log.warn("Электронная почта не может быть пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(user.getEmail())) {
            log.warn("Электронная почта должна содержать символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        if (!StringUtils.hasLength(user.getLogin()) || user.getLogin().contains(" ")) {
            log.warn("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
