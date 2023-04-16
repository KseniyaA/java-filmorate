package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private HashMap<Integer, User> users = new HashMap<>();
    private int idSequence = 0;

    @Override
    public User create(User user) {
        ValidationService.check(user);
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
        ValidationService.check(user);
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

    @Override
    public void addFriend(int userId, int friendId) {
        User user = get(userId);
        User friend = get(friendId);

        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = get(userId);
        User friend = get(friendId);

        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = get(userId);
        User other = get(otherId);
        Set<Integer> commonFriends = new HashSet<>(user.getFriends());
        commonFriends.retainAll(other.getFriends());
        return commonFriends.stream().map(this::get).collect(Collectors.toList());
    }

    @Override
    public List<User> getUserFriends(int userId) {
        User user = get(userId);
        return user.getFriends().stream().map(this::get).collect(Collectors.toList());
    }
}
