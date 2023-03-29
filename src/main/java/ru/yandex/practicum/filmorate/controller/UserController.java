package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public User create(@RequestBody User user) {
        log.info("Получен запрос POST /users с параметрами {}", user);
        return userService.create(user);
    }

    @PutMapping("/users")
    public User update(@RequestBody User user) {
        log.info("Получен запрос PUT /users с параметрами {}", user);
        return userService.update(user);
    }

    @DeleteMapping("/users")
    public void delete(@RequestBody User user) {
        log.info("Получен запрос DELETE /users с параметрами {}", user);
        userService.delete(user);
    }

    @GetMapping("users/{id}")
    public User get(@PathVariable int id) {
        log.info("Получен запрос GET /users/{id} с параметрами id = {}", id);
        return userService.get(id);
    }

    @GetMapping("/users")
    public List<User> findAll() {
        log.info("Получен запрос GET /users.");
        return userService.findAll();
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Получен запрос PUT /users/{id}/friends/{friendId} с параметрами id = {}, friendId = {}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Получен запрос DELETE /users/{id}/friends/{friendId} с параметрами id = {}, friendId = {}", userId, friendId);
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getFriends(@PathVariable("id") int userId) {
        log.info("Получен запрос GET /users/{id}/friends с параметрами id = {}", userId);
        return userService.getUserFriends(userId);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") int userId, @PathVariable int otherId) {
        log.info("Получен запрос GET /users/{id}/friends/common/{otherId} с параметрами id = {}, otherId = {}", userId, otherId);
        return userService.getCommonFriends(userId, otherId);
    }
}
