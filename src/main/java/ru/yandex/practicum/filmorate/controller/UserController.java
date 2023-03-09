package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
public class UserController {
    private HashMap<Integer, User> users = new HashMap<>();
    private int idSequence = 0;

    @GetMapping("/users")
    public List<User> findAll() {
        log.info("Получен запрос GET /users.");
        return new ArrayList<>(users.values());
    }

    @PostMapping("/users")
    public User create(@RequestBody User user) {
        log.info("Получен запрос POST /users с параметрами {}", user);
        check(user);
        user.setId(++idSequence);
        if (Optional.ofNullable(user.getName()).isEmpty()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping("/users")
    public User update(@RequestBody User user) {
        log.info("Получен запрос PUT /users с параметрами {}", user);
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id = {} не найден", user.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id = " + user.getId() + " не найден");
        }
        check(user);
        if (Optional.ofNullable(user.getName()).isEmpty()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    public HashMap<Integer, User> getUsers() {
        return users;
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
