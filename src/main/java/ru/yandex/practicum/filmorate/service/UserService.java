package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(User user) {
        userStorage.delete(user);
    }

    public User get(int id) {
        return userStorage.get(id);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    // добавление в друзья
    public void addFriend(int userId, int friendId) {
        User user = get(userId);
        User friend = get(friendId);

        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    // удаление из друзей
    public void removeFriend(int userId, int friendId) {
        User user = get(userId);
        User friend = get(friendId);

        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = get(userId);
        User other = get(otherId);
        Set<Integer> commonFriends = new HashSet<>(user.getFriends());
        commonFriends.retainAll(other.getFriends());
        return commonFriends.stream().map(this::get).collect(Collectors.toList());
    }

    public List<User> getUserFriends(int userId) {
        User user = get(userId);
        return user.getFriends().stream().map(this::get).collect(Collectors.toList());
    }
}
